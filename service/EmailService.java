package service;

import java.util.List;
import model.EmailMessage;
import model.HostConfiguration;


public class EmailService {
    private HostConfiguration hostConfig;

    public EmailService() {
    }

    public void setHostConfiguration(HostConfiguration hostConfig) {
        this.hostConfig = hostConfig;
    }

    public HostConfiguration getHostConfiguration() {
        return hostConfig;
    }

    // Send email
    public void sendEmail(EmailMessage message, HostConfiguration config) throws Exception {
        // TODO: Properties, Session, MimeMessage, Transport
    }

    // Receive emails from specific folder
    public List<EmailMessage> receiveEmails(String folderName, HostConfiguration config) throws Exception {
        // TODO: Properties, Session, Store, Folder
        return null;
    }

    // TODO: Mark email as read
    public void markAsRead(EmailMessage message) throws Exception {
    }

    // TODO: Delete email
    public void deleteEmail(EmailMessage message) throws Exception {
    }
}
