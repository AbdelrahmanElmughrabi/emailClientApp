package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.EmailMessage;
import service.EmailService;
import java.io.File;

/**
 * Controller for compose email window
 */
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
        // TODO: Setup attachment list display
    }

    @FXML
    private void handleAddAttachment() {
        // TODO: Open FileChooser, add file to attachmentList
    }

    @FXML
    private void handleRemoveAttachment() {
        // TODO: Remove selected attachment from list
    }

    @FXML
    private void handleSend() {
        // TODO: Create EmailMessage from fields
        // TODO: Call emailService.sendEmail()
        // Use new Thread(() -> {...}).start() for background send
    }

    @FXML
    private void handleCancel() {
        // TODO: Close window
        stage.close();
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Set up compose window for replying to an email
     * Pre-fills To field with sender and Subject with "Re: original subject"
     */
    public void setReplyTo(EmailMessage originalEmail) {
        if (originalEmail != null) {
            toField.setText(originalEmail.getFrom());
            String originalSubject = originalEmail.getSubject();
            if (!originalSubject.startsWith("Re: ")) {
                subjectField.setText("Re: " + originalSubject);
            } else {
                subjectField.setText(originalSubject);
            }
        }
    }
}
