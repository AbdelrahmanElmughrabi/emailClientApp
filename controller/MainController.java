package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.EmailMessage;
import model.HostConfiguration;
import service.EmailService;
import service.FolderManager;
import service.HostConfigManager;
import service.EmailCacheService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for main window (email list and reading pane)
 */
public class MainController {

    @FXML
    private TableView<EmailMessage> emailTable;

    @FXML
    private TextArea emailBodyArea;

    @FXML
    private Label fromLabel;

    @FXML
    private Label subjectLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private TreeView<String> folderTree;  // For displaying folders

    @FXML
    private ToggleButton cachingToggle;

    private EmailService emailService;
    private FolderManager folderManager;
    private HostConfigManager hostConfigManager;
    private final EmailCacheService emailCacheService = new EmailCacheService();

    private final ObservableList<EmailMessage> emailData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private volatile boolean isLoadingEmails = false;
    private volatile long lastRequestToken = 0;

    @FXML
    public void initialize() {
        // Setup email table columns
        TableColumn<EmailMessage, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFrom())
        );

        TableColumn<EmailMessage, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSubject())
        );

        TableColumn<EmailMessage, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getSentDate() != null
                                ? cell.getValue().getSentDate().format(dateFormatter)
                                : ""
                )
        );

        // Basic column sizing
        emailTable.getColumns().clear();
        emailTable.getColumns().addAll(fromCol, subjectCol, dateCol);
        fromCol.prefWidthProperty().bind(emailTable.widthProperty().multiply(0.3));
        subjectCol.prefWidthProperty().bind(emailTable.widthProperty().multiply(0.5));
        dateCol.prefWidthProperty().bind(emailTable.widthProperty().multiply(0.2));

        // Bind data list
        emailTable.setItems(emailData);

        // Selection listener to show details in reading pane
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showEmailDetails(newSel);
            } else {
                clearEmailDetails();
            }
        });

        // Initialize folder tree root (empty, will be filled after settings)
        TreeItem<String> rootItem = new TreeItem<>("Folders");
        rootItem.setExpanded(true);
        folderTree.setRoot(rootItem);

        // Folder selection listener: load messages when user selects folder
        folderTree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getParent() != null) {
                String folderName = newItem.getValue();
                loadEmailsForFolder(folderName);
            }
        });

        clearEmailDetails();
    }

    private void clearEmailDetails() {
        fromLabel.setText("");
        subjectLabel.setText("");
        dateLabel.setText("");
        emailBodyArea.clear();
    }

    private void showEmailDetails(EmailMessage email) {
        if (email == null) {
            clearEmailDetails();
            return;
        }

        fromLabel.setText(email.getFrom() != null ? email.getFrom() : "");
        subjectLabel.setText(email.getSubject() != null ? email.getSubject() : "");
        if (email.getSentDate() != null) {
            dateLabel.setText(email.getSentDate().format(dateFormatter));
        } else {
            dateLabel.setText("");
        }
        
        // Lazy Load Body
        if (email.getBody() == null) {
            emailBodyArea.setText("Loading content...");
            
            // Capture necessary data for thread
            String messageId = email.getMessageId();
            String folderName = email.getFolder();
            HostConfiguration config = requireHostConfig();
            
            // Don't spawn thread if essential data is missing
            if (messageId == null || folderName == null || config == null) {
                 emailBodyArea.setText("Error: Missing message info.");
                 return;
            }

            new Thread(() -> {
                try {
                    String body = emailService.fetchEmailBody(messageId, folderName, config);
                    
                    // Update UI and Cache in Memory
                    Platform.runLater(() -> {
                        // verify user hasn't clicked another email while we were loading
                        EmailMessage currentSelection = emailTable.getSelectionModel().getSelectedItem();
                        if (currentSelection != null && currentSelection.getMessageId().equals(messageId)) {
                             email.setBody(body);
                             emailBodyArea.setText(body);
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        emailBodyArea.setText("Error loading body: " + ex.getMessage());
                    });
                }
            }).start();
        } else {
            // Already loaded
            emailBodyArea.setText(email.getBody());
        }
    }

    private HostConfiguration requireHostConfig() {
        HostConfiguration config = null;
        if (hostConfigManager != null) {
            config = hostConfigManager.getCurrentHost();
        }
        if (config == null && emailService != null) {
            config = emailService.getHostConfiguration();
        }

        if (config == null) {
            showError("No host configuration",
                    "Please configure your email server settings first (Settings button).");
        }
        return config;
    }

    private void loadFolders() {
        HostConfiguration config = requireHostConfig();
        if (config == null) {
            return;
        }

        new Thread(() -> {
            try {
                List<String> folders = folderManager.getFolders(config);
                Platform.runLater(() -> {
                    TreeItem<String> root = new TreeItem<>("Folders");
                    root.setExpanded(true);
                    for (String name : folders) {
                        root.getChildren().add(new TreeItem<>(name));
                    }
                    folderTree.setRoot(root);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError("Error loading folders", ex.getMessage())
                );
            }
        }).start();
    }

    private void loadEmailsForFolder(String folderName) {
        // Remove blocking check to allow folder switching.
        // The 'lastRequestToken' mechanism handles concurrency by discarding stale results.

        HostConfiguration config = requireHostConfig();
        if (config == null) {
            return;
        }

        isLoadingEmails = true;
        // Generate a new token for this specific request
        long currentToken = System.currentTimeMillis();
        lastRequestToken = currentToken;
        boolean useCache = cachingToggle.isSelected();

        // IMMEDIATE UI FEEDBACK: Clear old data instantly
        Platform.runLater(() -> {
            emailData.clear();
            clearEmailDetails();
            subjectLabel.setText("Loading " + folderName + "...");
        });

        new Thread(() -> {
            try {
                if (useCache) {
                    List<EmailMessage> cachedMessages = emailCacheService.loadEmails(folderName);
                    if (!cachedMessages.isEmpty()) {
                        Platform.runLater(() -> {
                            if (lastRequestToken == currentToken) {
                                emailData.setAll(cachedMessages);
                                subjectLabel.setText(""); // Clear loading text
                            }
                        });
                    }
                }

                // Fetch from Server (Network) - Now fast because we skip bodies!
                List<EmailMessage> freshMessages = emailService.receiveEmails(folderName, config);

                if (useCache) {
                    emailCacheService.saveEmails(folderName, freshMessages);
                }

                // Update UI - ONLY if this is still the latest request
                Platform.runLater(() -> {
                    if (lastRequestToken == currentToken) {
                        emailData.setAll(freshMessages);
                        clearEmailDetails();
                        subjectLabel.setText("");
                    } else {
                        System.out.println("Discarding stale email data (newer request exists)");
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    if (lastRequestToken == currentToken) {
                        showError("Error loading emails", ex.getMessage());
                        subjectLabel.setText("");
                    }
                });
            } finally {
                if (lastRequestToken == currentToken) {
                    isLoadingEmails = false;
                }
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        if (isLoadingEmails) {
            // Debounce: Do not allow refreshing if already loading
            return;
        }
        
        // Refresh currently selected folder; default to INBOX
        TreeItem<String> selected = folderTree.getSelectionModel().getSelectedItem();
        String folderName = "INBOX";
        if (selected != null && selected.getParent() != null) {
            folderName = selected.getValue();
        }
        loadEmailsForFolder(folderName);
    }

    @FXML
    private void handleCompose() {
        if (emailService == null) {
            showError("Email service not initialized", "Internal error: emailService is null.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/ComposeWindow.fxml"));
            Parent root = loader.load();

            ComposeController composeController = loader.getController();
            composeController.setEmailService(emailService);

            Stage stage = new Stage();
            composeController.setStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Compose Email");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error opening compose window", ex.getMessage());
        }
    }

    @FXML
    private void handleReply() {
        EmailMessage selected = emailTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No email selected", "Please select an email to reply to.");
            return;
        }

        if (emailService == null) {
            showError("Email service not initialized", "Internal error: emailService is null.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/ComposeWindow.fxml"));
            Parent root = loader.load();

            ComposeController composeController = loader.getController();
            composeController.setEmailService(emailService);
            composeController.setReplyTo(selected);

            Stage stage = new Stage();
            composeController.setStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Reply");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error opening reply window", ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        EmailMessage selected = emailTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No email selected", "Please select an email to delete.");
            return;
        }

        HostConfiguration config = hostConfigManager.getCurrentHost();
        if (config == null) {
            showError("No account configured", "Please configure an email account first.");
            return;
        }

        // Optimistic Update: Remove from UI immediately
        int selectedIndex = emailTable.getSelectionModel().getSelectedIndex();
        emailData.remove(selected);

        // Run network operation in background
        new Thread(() -> {
            try {
                emailService.deleteEmail(selected, config);
            } catch (Exception ex) {
                ex.printStackTrace();
                // If failed, restore the email to UI
                Platform.runLater(() -> {
                    emailData.add(selectedIndex, selected);
                    showError("Error deleting email", "Failed to delete from server. Restoring email.\n" + ex.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/HostConfigWindow.fxml"));
            Parent root = loader.load();

            HostConfigController configController = loader.getController();
            configController.setEmailService(emailService);
            configController.setHostConfigManager(hostConfigManager);

            Stage stage = new Stage();
            configController.setStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Server Settings");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // After closing settings, reload folders
            loadFolders();

            // Persist configuration if possible
            try {
                hostConfigManager.saveToFile("host_config.dat");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error opening settings", ex.getMessage());
        }
    }

    @FXML
    private void handleCachingToggle() {
        if (cachingToggle.isSelected()) {
            cachingToggle.setText("Caching: ON");
        } else {
            cachingToggle.setText("Caching: OFF");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message != null ? message : "");
        alert.showAndWait();
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setFolderManager(FolderManager folderManager) {
        this.folderManager = folderManager;
    }

    public void setHostConfigManager(HostConfigManager hostConfigManager) {
        this.hostConfigManager = hostConfigManager;
    }
}
