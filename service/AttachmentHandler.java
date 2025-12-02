package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;

public class AttachmentHandler {

    public AttachmentHandler() {
    }

    // Convert File to MimeBodyPart for email attachment
    public MimeBodyPart createAttachmentPart(File file) throws Exception {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        FileDataSource source = new FileDataSource(file);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(file.getName());
        return attachmentPart;
    }

    // Save MimeBodyPart attachment to disk
    public File saveAttachment(MimeBodyPart part, String destinationFolder) throws Exception {
        String fileName = part.getFileName();
        if (fileName == null) {
            fileName = "attachment_" + System.currentTimeMillis();
        }

        // Create destination directory if needed
        File destDir = new File(destinationFolder);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File outputFile = new File(destDir, fileName);

        // Write attachment to file
        try (InputStream input = part.getInputStream();
             FileOutputStream output = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        return outputFile;
    }

    public long getFileSize(File file) {
        return file.length();
    }
}
