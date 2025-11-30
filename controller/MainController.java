package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import model.EmailMessage;
import service.EmailService;
import service.FolderManager;

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

    @FXML
    public void initialize() {
        // TODO: Initialize table columns
        // TODO: Set up email selection listener
        // TODO: Load folders into folderTree using folderManager.getFolders()
        // TODO: Add listener to folderTree selection - load emails from selected folder
    }

    @FXML
    private void handleRefresh() {
        // TODO: Fetch emails using emailService.receiveEmails()
        // Use Platform.runLater() for GUI updates
    }

    @FXML
    private void handleCompose() {
        // TODO: Open compose window
    }

    @FXML
    private void handleReply() {
        // TODO: Get selected email, open compose window in reply mode
        // EmailMessage selected = emailTable.getSelectionModel().getSelectedItem();
        // if (selected != null) {
        //     // Open ComposeWindow and call setReplyTo(selected)
        // }
    }

    @FXML
    private void handleDelete() {
        // TODO: Delete selected email
    }

    @FXML
    private void handleSettings() {
        // TODO: Open host configuration window
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setFolderManager(FolderManager folderManager) {
        this.folderManager = folderManager;
    }
}
