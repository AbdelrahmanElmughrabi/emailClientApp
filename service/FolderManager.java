package service;

import model.HostConfiguration;
import model.EmailMessage;
import java.util.List;

/**
 * Manages email folders (INBOX, Sent Items, Trial folder)
 */
public class FolderManager {

    public FolderManager() {
    }

    // Get list of folder names from server
    public List<String> getFolders(HostConfiguration config) throws Exception {
        // TODO: Use Week 7 pattern - store.getDefaultFolder().list()
        return null;
    }

    // Get all messages from a specific folder
    public List<EmailMessage> getMessagesFromFolder(String folderName, HostConfiguration config) throws Exception {
        // TODO: Use Week 7 pattern - store.getFolder(folderName).getMessages()
        return null;
    }

    // Optional: Move message to another folder
    public void moveMessage(EmailMessage message, String targetFolder, HostConfiguration config) throws Exception {
        // TODO: Copy to target folder, delete from source
    }
}
