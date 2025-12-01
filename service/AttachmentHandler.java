package service;

import java.io.File;
import javax.mail.internet.MimeBodyPart;


public class AttachmentHandler {

    public AttachmentHandler() {
    }

    // Create attachment part (used BY EmailService when sending)
    public MimeBodyPart createAttachmentPart(File file) throws Exception {
        // TODO: MimeBodyPart, FileDataSource
        return null;
    }

    // Save attachment from email (receives MimeBodyPart FROM EmailService)
    public File saveAttachment(MimeBodyPart part, String destinationFolder) throws Exception {
        // TODO: part.saveFile() or part.getInputStream()
        return null;
    }

    // Helper: Get file size
    public long getFileSize(File file) {
        return file.length();
    }
}
