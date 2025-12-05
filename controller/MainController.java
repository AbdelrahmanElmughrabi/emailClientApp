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

    private EmailService emailService;
    private FolderManager folderManager;
    private HostConfigManager hostConfigManager;

    private final ObservableList<EmailMessage> emailData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        emailBodyArea.setText(email.getBody() != null ? email.getBody() : "");
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
        HostConfiguration config = requireHostConfig();
        if (config == null) {
            return;
        }

        new Thread(() -> {
            try {
                List<EmailMessage> messages = emailService.receiveEmails(folderName, config);
                Platform.runLater(() -> {
                    emailData.setAll(messages);
                    clearEmailDetails();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError("Error loading emails", ex.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
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

        try {
            // Delete from server
            emailService.deleteEmail(selected, config);
            // Remove from UI
            emailData.remove(selected);
            // Refresh to sync with server
            handleRefresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error deleting email", ex.getMessage());
        }
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
