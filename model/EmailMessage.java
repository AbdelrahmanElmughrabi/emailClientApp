package model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model for an email message
 */
public class EmailMessage {
    private String from;
    private List<String> to;
    private String subject;
    private String body;
    private LocalDateTime sentDate;
    private boolean isRead;
    private List<File> attachments;
    private String folder;

    public EmailMessage() {
        this.to = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.isRead = false;
    }

    // Getters and Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public void addTo(String recipient) {
        this.to.add(recipient);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(File file) {
        this.attachments.add(file);
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return "EmailMessage{" +
                "from='" + from + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate=" + sentDate +
                ", isRead=" + isRead +
                '}';
    }
}
