package service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import model.EmailMessage;
import model.HostConfiguration;

public class EmailService {

    private HostConfiguration hostConfig;
    private AttachmentHandler attachmentHandler;

    public EmailService() {
        this.attachmentHandler = new AttachmentHandler();
    }

    public void setHostConfiguration(HostConfiguration hostConfig) {
        this.hostConfig = hostConfig;
    }

    public HostConfiguration getHostConfiguration() {
        return hostConfig;
    }

    public void sendEmail(EmailMessage message, HostConfiguration config) throws Exception {
        // Configure SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSendHost());
        props.put("mail.smtp.port", config.getSendPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        // Create authenticated session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        });

        // Build MIME message
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(message.getFrom()));

        for (String recipient : message.getTo()) {
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }

        mimeMessage.setSubject(message.getSubject());

        // Handle attachments if present
        if (message.hasAttachments()) {
            Multipart multipart = new MimeMultipart();

            // Add text body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message.getBody());
            multipart.addBodyPart(textPart);

            // Add each attachment
            for (File attachment : message.getAttachments()) {
                MimeBodyPart attachmentPart = attachmentHandler.createAttachmentPart(attachment);
                multipart.addBodyPart(attachmentPart);
            }

            mimeMessage.setContent(multipart);
        } else {
            mimeMessage.setText(message.getBody());
        }

        Transport.send(mimeMessage);
    }

    public List<EmailMessage> receiveEmails(String folderName, HostConfiguration config) throws Exception {
        // Configure mail store properties
        Properties props = new Properties();
        props.put("mail.store.protocol", config.getReceiveProtocol());
        props.put("mail." + config.getReceiveProtocol() + ".host", config.getReceiveHost());
        props.put("mail." + config.getReceiveProtocol() + ".port", config.getReceivePort());
        props.put("mail." + config.getReceiveProtocol() + ".ssl.enable", "true");

        // Connect to mail store
        Session session = Session.getInstance(props);
        Store store = session.getStore(config.getReceiveProtocol());
        store.connect(config.getReceiveHost(), config.getUsername(), config.getPassword());

        // Open folder and fetch messages
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);

        // Fetch only last 20 messages for performance
        int messageCount = folder.getMessageCount();
        Message[] messages;
        if (messageCount > 20) {
            messages = folder.getMessages(messageCount - 19, messageCount);
        } else {
            messages = folder.getMessages();
        }

        List<EmailMessage> emailList = new ArrayList<>();

        // Convert JavaMail messages to EmailMessage objects
        for (Message msg : messages) {
            // DEBUG: Print flags for investigation
            // System.out.println("Msg Subject: " + msg.getSubject() + " | Flags: " + java.util.Arrays.toString(msg.getFlags().getSystemFlags()) + " | UserFlags: " + java.util.Arrays.toString(msg.getFlags().getUserFlags()));

            if (msg.isSet(Flags.Flag.DELETED)) {
                continue;
            }
            EmailMessage email = new EmailMessage();

            Address[] fromAddresses = msg.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                email.setFrom(fromAddresses[0].toString());
            }

            email.setSubject(msg.getSubject());
            email.setBody(getTextFromMessage(msg));

            Date sentDate = msg.getSentDate();
            if (sentDate != null) {
                email.setSentDate(LocalDateTime.ofInstant(sentDate.toInstant(), ZoneId.systemDefault()));
            }

            email.setRead(msg.isSet(Flags.Flag.SEEN));
            email.setFolder(folderName);

            // Store message ID for deletion
            String[] messageIdHeaders = msg.getHeader("Message-ID");
            if (messageIdHeaders != null && messageIdHeaders.length > 0) {
                email.setMessageId(messageIdHeaders[0]);
            }

            emailList.add(email);
        }

        folder.close(false);
        store.close();

        return emailList;
    }

    // Extract text content from message (handles plain and multipart)
    private String getTextFromMessage(Message message) throws Exception {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    // Recursively extract text from multipart messages
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    public void markAsRead(EmailMessage message) throws Exception {
        // TODO: Implementation needed
    }

    public void deleteEmail(EmailMessage message, HostConfiguration config) throws Exception {
        if (message.getMessageId() == null) {
            throw new Exception("Cannot delete email: Message ID is missing");
        }

        // Configure mail store properties
        Properties props = new Properties();
        props.put("mail.store.protocol", config.getReceiveProtocol());
        props.put("mail." + config.getReceiveProtocol() + ".host", config.getReceiveHost());
        props.put("mail." + config.getReceiveProtocol() + ".port", config.getReceivePort());
        props.put("mail." + config.getReceiveProtocol() + ".ssl.enable", "true");

        // Connect to mail store
        Session session = Session.getInstance(props);
        Store store = session.getStore(config.getReceiveProtocol());
        store.connect(config.getReceiveHost(), config.getUsername(), config.getPassword());

        // Open folder in READ_WRITE mode
        Folder folder = store.getFolder(message.getFolder());
        folder.open(Folder.READ_WRITE);

        // Find the message by Message-ID header
        Message[] messages = folder.getMessages();
        for (Message msg : messages) {
            String[] messageIdHeaders = msg.getHeader("Message-ID");
            if (messageIdHeaders != null && messageIdHeaders.length > 0) {
                if (messageIdHeaders[0].equals(message.getMessageId())) {
                    // Mark message as deleted
                    msg.setFlag(Flags.Flag.DELETED, true);
                    break;
                }
            }
        }

        // Close folder with expunge=true to permanently delete
        folder.close(true);
        store.close();
    }
}
