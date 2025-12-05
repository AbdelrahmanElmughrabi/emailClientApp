package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.HostConfiguration;
import service.EmailService;
import service.HostConfigManager;



//Controller for host configuration window

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
        // Setup protocol choice (IMAP/POP3)
        protocolChoice.getItems().addAll("imap", "pop3");
        protocolChoice.setValue("imap");

        // Set default ports
        receivePortField.setText("993");  // IMAP SSL
        sendPortField.setText("465");     // SMTP SSL
    }

    @FXML
    private void handleSave() {
        try {
            HostConfiguration config = buildConfigFromFields();

            if (hostConfigManager != null) {
                hostConfigManager.addHost(config);
                hostConfigManager.setCurrentHost(config);
            }

            if (emailService != null) {
                emailService.setHostConfiguration(config);
            }

            // Save to file is done from MainController; no need to do here
            stage.close();
        } catch (IllegalArgumentException ex) {
            showError("Invalid input", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error", ex.getMessage());
        }
    }

    @FXML
    private void handleTest() {
        try {
            HostConfiguration config = buildConfigFromFields();

            // Basic test: try listing folders (INBOX etc.)
            if (emailService == null) {
                showError("Email service not available", "Internal error: emailService is null.");
                return;
            }

            emailService.setHostConfiguration(config);

            // Simple test: attempt to receive from INBOX but ignore result
            emailService.receiveEmails("INBOX", config);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Test successful");
            ok.setHeaderText("Connection OK");
            ok.setContentText("Successfully connected to server with given settings.");
            ok.showAndWait();

        } catch (IllegalArgumentException ex) {
            showError("Invalid input", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Connection failed", ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private HostConfiguration buildConfigFromFields() {
        String protocol = protocolChoice.getValue();
        String recvHost = receiveHostField.getText().trim();
        String recvPortText = receivePortField.getText().trim();
        String sendHost = sendHostField.getText().trim();
        String sendPortText = sendPortField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (recvHost.isEmpty() || sendHost.isEmpty() || username.isEmpty()) {
            throw new IllegalArgumentException("Host and username fields cannot be empty.");
        }

        int recvPort;
        int sendPort;
        try {
            recvPort = Integer.parseInt(recvPortText);
            sendPort = Integer.parseInt(sendPortText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ports must be valid integers.");
        }

        HostConfiguration config = new HostConfiguration();
        config.setReceiveProtocol(protocol);
        config.setReceiveHost(recvHost);
        config.setReceivePort(recvPort);
        config.setSendHost(sendHost);
        config.setSendPort(sendPort);
        config.setUsername(username);
        config.setPassword(password);

        return config;
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

    public void setHostConfigManager(HostConfigManager hostConfigManager) {
        this.hostConfigManager = hostConfigManager;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
