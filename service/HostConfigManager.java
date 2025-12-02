package service;

import model.HostConfiguration;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HostConfigManager {
    private List<HostConfiguration> savedHosts;
    private HostConfiguration currentHost;

    public HostConfigManager() {
        this.savedHosts = new ArrayList<>();
    }

    public void addHost(HostConfiguration host) {
        if (!savedHosts.contains(host)) {
            savedHosts.add(host);
        }
    }

    public List<HostConfiguration> getAllHosts() {
        return savedHosts;
    }

    public HostConfiguration getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(HostConfiguration host) {
        this.currentHost = host;
        if (!savedHosts.contains(host)) {
            savedHosts.add(host);
        }
    }

    public void saveToFile(String filepath) throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filepath))) {
            out.writeObject(savedHosts);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filepath) throws Exception {
        File file = new File(filepath);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filepath))) {
            savedHosts = (List<HostConfiguration>) in.readObject();
            if (!savedHosts.isEmpty()) {
                currentHost = savedHosts.get(0);
            }
        }
    }
}
