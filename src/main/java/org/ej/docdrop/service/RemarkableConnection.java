package org.ej.docdrop.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Abstracts avoid lots of mocking, boundary with external system.
 */
@Component
class RemarkableConnection {

    private final SSHClient sshClient;

    public RemarkableConnection() throws IOException {

        sshClient = new SSHClient();
        sshClient.loadKnownHosts();
        sshClient.setConnectTimeout(200);
    }

    private void connect() throws ConnectionException {
        if (sshClient.isConnected() && sshClient.isAuthenticated()) {
            return;
        }

        try {
            sshClient.connect("192.168.2.2");
        } catch (IOException e) {
            throw new ConnectionException("Trouble connecting to Remarkable", e);
        }

        try {
            sshClient.authPassword("root", "ZjZQdup7xQ");
        } catch (UserAuthException e) {
            throw new ConnectionException("Failed to authenticate", e);
        } catch (TransportException e) {
            throw new ConnectionException("Trouble with connection during authentication", e);
        }
    }

    List<RemoteResourceInfo> readFileTree() throws ConnectionException {
        connect();


        SFTPClient sftpClient;
        try {
            sftpClient = sshClient.newSFTPClient();
        } catch (IOException e) {
            throw new ConnectionException("Trouble creating SFTP client", e);
        }

        List<RemoteResourceInfo> listing;
        try {
            listing = sftpClient.ls(
                    "/home/root/.local/share/remarkable/xochitl",
                    resource -> resource.getName().endsWith("metadata"));
        } catch (IOException e) {
            throw new ConnectionException("Trouble listing files", e);
        }

        // Should throw from this point, or just ignore / log?
        try {
            sftpClient.close();
        } catch (IOException e) {
            throw new ConnectionException("Error during closing of sftClient", e);
        }

        return listing;
    }

    List<RemoteResourceInfo> readFileTree2() throws ConnectionException {
        final SSHClient sshClient = new SSHClient();
        sshClient.setConnectTimeout(100);


        try {
            sshClient.loadKnownHosts();
        } catch (IOException e) {
            throw new ConnectionException("Trouble loading known hosts", e);
        }

        try {
            sshClient.connect("192.168.2.6");
        } catch (IOException e) {
            throw new ConnectionException("Trouble connecting to Remarkable", e);
        }

        try {
            sshClient.authPassword("root", "ZjZQdup7xQ");
        } catch (UserAuthException e) {
            throw new ConnectionException("Failed to authenticate", e);
        } catch (TransportException e) {
            throw new ConnectionException("Trouble with connection during authentication", e);
        }

        SFTPClient sftpClient;
        try {
            sftpClient = sshClient.newSFTPClient();
        } catch (IOException e) {
            throw new ConnectionException("Trouble creating SFTP client", e);
        }

        List<RemoteResourceInfo> listing;
        try {
            listing = sftpClient.ls(
                    "/home/root/.local/share/remarkable/xochitl",
                    resource -> resource.getName().endsWith("metadata"));
        } catch (IOException e) {
            throw new ConnectionException("Trouble listing files", e);
        }

        // Should throw from this point, or just ignore / log?
        try {
            sftpClient.close();
        } catch (IOException e) {
            throw new ConnectionException("Error during closing of sftClient", e);
        }

        try {
            sshClient.close();
        } catch (IOException e) {
            throw new ConnectionException("Error closing ssh connection", e);
        }


        return listing;
    }

    String readFile(String path) throws ConnectionException {
        connect();

        SFTPClient sftpClient;
        try {
            sftpClient = sshClient.newSFTPClient();
        } catch (IOException e) {
            throw new ConnectionException("Trouble creating SFTP client", e);
        }

        RemoteFile file;
        try {
            file = sftpClient.open(path);
        } catch (IOException e) {
            throw new ConnectionException("Error opening file: " + path, e);
        }

        int length = 0;
        try {
            length = (int) file.length();
        } catch (IOException e) {
            throw new ConnectionException("Error determining length of: " + file, e);
        }
        byte[] contents = new byte[length];
        try {
            file.read(0, contents, 0, length);
        } catch (IOException e) {
            throw new ConnectionException("Error reading file: " + file, e);
        }

        try {
            file.close();
        } catch (IOException e) {
            throw new ConnectionException("Error closing file: " + file, e);
        }

        return new String(contents, StandardCharsets.UTF_8);
    }

    String readFile2(String path) throws ConnectionException {
        final SSHClient sshClient = new SSHClient();
        sshClient.setConnectTimeout(100);
        try {
            sshClient.loadKnownHosts();
        } catch (IOException e) {
            throw new ConnectionException("Trouble loading known hosts", e);
        }

        try {
            sshClient.connect("192.168.2.6");
        } catch (IOException e) {
            throw new ConnectionException("Trouble connecting to Remarkable", e);
        }

        try {
            sshClient.authPassword("root", "ZjZQdup7xQ");
        } catch (UserAuthException e) {
            throw new ConnectionException("Failed to authenticate", e);
        } catch (TransportException e) {
            throw new ConnectionException("Trouble with connection during authentication", e);
        }

        SFTPClient sftpClient;
        try {
            sftpClient = sshClient.newSFTPClient();
        } catch (IOException e) {
            throw new ConnectionException("Trouble creating SFTP client", e);
        }

        RemoteFile file;
        try {
            file = sftpClient.open(path);
        } catch (IOException e) {
            throw new ConnectionException("Error opening file: " + path, e);
        }

        int length = 0;
        try {
            length = (int) file.length();
        } catch (IOException e) {
            throw new ConnectionException("Error determining length of: " + file, e);
        }
        byte[] contents = new byte[length];
        try {
            file.read(0, contents, 0, length);
        } catch (IOException e) {
            throw new ConnectionException("Error reading file: " + file, e);
        }

        try {
            file.close();
        } catch (IOException e) {
            throw new ConnectionException("Error closing file: " + file, e);
        }

        return new String(contents, StandardCharsets.UTF_8);
    }
}