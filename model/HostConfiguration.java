package model;

/**
 * Data model for email server host configuration
 * Stores SMTP and IMAP/POP3 server settings
 */
public class HostConfiguration {
    private String receiveProtocol;  // "imap" or "pop3"
    private String receiveHost;
    private int receivePort;         // 993 for IMAP, 995 for POP3

    private String sendHost;         // SMTP host
    private int sendPort;            // 465 (SSL)

    private String username;
    private String password;

    public HostConfiguration() {
        // Default values
        this.receiveProtocol = "imap";
        this.receivePort = 993;
        this.sendPort = 465;
    }

    // Getters and Setters
    public String getReceiveProtocol() {
        return receiveProtocol;
    }

    public void setReceiveProtocol(String receiveProtocol) {
        this.receiveProtocol = receiveProtocol;
    }

    public String getReceiveHost() {
        return receiveHost;
    }

    public void setReceiveHost(String receiveHost) {
        this.receiveHost = receiveHost;
    }

    public int getReceivePort() {
        return receivePort;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    public String getSendHost() {
        return sendHost;
    }

    public void setSendHost(String sendHost) {
        this.sendHost = sendHost;
    }

    public int getSendPort() {
        return sendPort;
    }

    public void setSendPort(int sendPort) {
        this.sendPort = sendPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "HostConfiguration{" +
                "receiveProtocol='" + receiveProtocol + '\'' +
                ", receiveHost='" + receiveHost + '\'' +
                ", sendHost='" + sendHost + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
