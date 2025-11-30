package service;

import java.io.File;


public class AttachmentHandler {

    public AttachmentHandler() {
    }

    // Add attachment using MimeBodyPart
    public void addAttachment(File file) throws Exception {
        // Will use: MimeBodyPart, FileDataSource
    }

   //Save attachment from email
    public void saveAttachment(String attachmentName, String destinationPath) throws Exception {
        // Will use: Part.saveFile()
    }

    // Helper: Get file size
    public long getFileSize(File file) {
        return file.length();
    }
}
