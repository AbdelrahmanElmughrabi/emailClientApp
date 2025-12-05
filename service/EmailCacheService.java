package service;

import model.EmailMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmailCacheService {

    private static final String CACHE_DIR = "email_cache";

    public EmailCacheService() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public void saveEmails(String folderName, List<EmailMessage> emails) {
        // Sanitize folder name for file system
        String filename = getCacheFileName(folderName);
        File cacheFile = new File(CACHE_DIR, filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            oos.writeObject(emails);
        } catch (IOException e) {
            System.err.println("Failed to save cache for folder " + folderName + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<EmailMessage> loadEmails(String folderName) {
        String filename = getCacheFileName(folderName);
        File cacheFile = new File(CACHE_DIR, filename);

        if (!cacheFile.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
            return (List<EmailMessage>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load cache for folder " + folderName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getCacheFileName(String folderName) {
        // Replace potentially invalid characters
        return folderName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".dat";
    }
}
