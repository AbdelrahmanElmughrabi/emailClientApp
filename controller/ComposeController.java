package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.EmailMessage;
import model.HostConfiguration;
import service.EmailService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


 // Controller for compose email window
public class ComposeController {

    @FXML
    private TextField toField;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea bodyArea;

    @FXML
    private ListView<File> attachmentList;

    private EmailService emailService;
    private Stage stage;

    @FXML
    public void initialize() {
        // Show file name only in the list
        attachmentList.setCellFactory(listView ->
                new javafx.scene.control.ListCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                }
        );
    }

    @FXML
    private void handleAddAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachments");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            attachmentList.getItems().addAll(files);
        }
    }

    @FXML
    private void handleRemoveAttachment() {
        File selected = attachmentList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            attachmentList.getItems().remove(selected);
        }
    }

    @FXML
    private void handleSend() {
        if (emailService == null) {
            showError("Email service not initialized", "Internal error: emailService is null.");
            return;
        }

        HostConfiguration config = emailService.getHostConfiguration();
        if (config == null) {
            showError("No host configuration", "Please configure server settings before sending.");
            return;
        }

        String toText = toField.getText().trim();
        if (toText.isEmpty()) {
            showError("Missing recipient", "Please enter at least one recipient.");
            return;
        }

        EmailMessage message = new EmailMessage();
        message.setFrom(config.getUsername()); // From = account username

        // Split TO by comma/semicolon
        List<String> recipients = new ArrayList<>();
        Arrays.stream(toText.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(recipients::add);
        message.setTo(recipients);

        message.setSubject(subjectField.getText());
        message.setBody(bodyArea.getText());
        message.setSentDate(LocalDateTime.now());

        // Attachments
        message.setAttachments(new ArrayList<>(attachmentList.getItems()));

        // Send in background
        new Thread(() -> {
            try {
                emailService.sendEmail(message, config);
                Platform.runLater(() -> {
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Email sent");
                    ok.setHeaderText("Success");
                    ok.setContentText("Email has been sent.");
                    ok.showAndWait();
                    stage.close();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showError("Error sending email", ex.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        stage.close();
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

     // Set up compose window for replying to an email
     // Pre-fills To field with sender and Subject with "Re: original subject"
     
    public void setReplyTo(EmailMessage originalEmail) {
        if (originalEmail != null) {
            toField.setText(originalEmail.getFrom());
            String originalSubject = originalEmail.getSubject();
            if (originalSubject == null) {
                originalSubject = "";
            }
            if (!originalSubject.startsWith("Re: ")) {
                subjectField.setText("Re: " + originalSubject);
            } else {
                subjectField.setText(originalSubject);
            }
        }
    }
}

