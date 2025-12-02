package service;

import model.HostConfiguration;
import model.EmailMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;

public class FolderManager {
    private EmailService emailService;

    public FolderManager() {
        this.emailService = new EmailService();
    }

    // Retrieve list of all folders from mail server
    public List<String> getFolders(HostConfiguration config) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", config.getReceiveProtocol());
        props.put("mail." + config.getReceiveProtocol() + ".host", config.getReceiveHost());
        props.put("mail." + config.getReceiveProtocol() + ".port", config.getReceivePort());
        props.put("mail." + config.getReceiveProtocol() + ".ssl.enable", "true");

        // Connect and list folders
        Session session = Session.getInstance(props);
        Store store = session.getStore(config.getReceiveProtocol());
        store.connect(config.getReceiveHost(), config.getUsername(), config.getPassword());

        Folder defaultFolder = store.getDefaultFolder();
        Folder[] folders = defaultFolder.list();

        List<String> folderNames = new ArrayList<>();
        for (Folder folder : folders) {
            folderNames.add(folder.getName());
        }

        store.close();
        return folderNames;
    }

    // Get messages from specific folder (delegates to EmailService)
    public List<EmailMessage> getMessagesFromFolder(String folderName, HostConfiguration config) throws Exception {
        return emailService.receiveEmails(folderName, config);
    }

    public void moveMessage(EmailMessage message, String targetFolder, HostConfiguration config) throws Exception {
        // TODO: Implementation for moving messages
    }
}
