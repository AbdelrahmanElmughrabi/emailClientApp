package service;

import model.EmailMessage;
import model.HostConfiguration;
import java.util.List;


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
    public void sendEmail(EmailMessage message) throws Exception {
        // Will use: Properties, Session, MimeMessage, Transport
    }

    // Receive emails
    public List<EmailMessage> receiveEmails() throws Exception {
        // Will use: Properties, Session, Store, Folder
        return null;
    }

    // TODO: Mark email as read
    public void markAsRead(EmailMessage message) throws Exception {
    }

    // TODO: Delete email
    public void deleteEmail(EmailMessage message) throws Exception {
    }
}
