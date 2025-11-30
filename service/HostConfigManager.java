package service;

import model.HostConfiguration;
import java.util.ArrayList;
import java.util.List;

/**
 * Save/load multiple email account configurations
 */
public class HostConfigManager {
    private List<HostConfiguration> savedHosts;
    private HostConfiguration currentHost;

    public HostConfigManager() {
        this.savedHosts = new ArrayList<>();
    }

    // Add new host configuration
    public void addHost(HostConfiguration host) {
        // TODO: Add to list, optionally save to file
    }

    // Get all saved hosts
    public List<HostConfiguration> getAllHosts() {
        return savedHosts;
    }

    // Get currently active host
    public HostConfiguration getCurrentHost() {
        return currentHost;
    }

    // Set current active host
    public void setCurrentHost(HostConfiguration host) {
        this.currentHost = host;
    }

    // Optional: Save to file
    public void saveToFile(String filepath) throws Exception {
        // TODO: Serialize to properties file or JSON
    }

    // Optional: Load from file
    public void loadFromFile(String filepath) throws Exception {
        // TODO: Deserialize from file
    }
}
