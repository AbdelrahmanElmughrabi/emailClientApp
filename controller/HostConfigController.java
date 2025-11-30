package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import model.HostConfiguration;
import service.EmailService;
import service.HostConfigManager;

/**
 * Controller for host configuration window
 */
public class HostConfigController {

    @FXML
    private ChoiceBox<String> protocolChoice;

    @FXML
    private TextField receiveHostField;

    @FXML
    private TextField receivePortField;

    @FXML
    private TextField sendHostField;

    @FXML
    private TextField sendPortField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private EmailService emailService;
    private HostConfigManager hostConfigManager;
    private Stage stage;

    @FXML
    public void initialize() {
        // TODO: Setup protocol choice (IMAP/POP3)
        protocolChoice.getItems().addAll("imap", "pop3");
        protocolChoice.setValue("imap");

        // TODO: Set default ports
        receivePortField.setText("993");
        sendPortField.setText("465");
    }

    @FXML
    private void handleSave() {
        // TODO: Create HostConfiguration from fields
        // TODO: Call hostConfigManager.addHost(config)
        // TODO: Call hostConfigManager.setCurrentHost(config)
        stage.close();
    }

    @FXML
    private void handleTest() {
        // TODO: Test connection with current settings
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setHostConfigManager(HostConfigManager hostConfigManager) {
        this.hostConfigManager = hostConfigManager;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
