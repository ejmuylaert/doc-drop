package org.ej.docdrop.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Abstracts avoid lots of mocking, boundary with external system.
 */
@Component
class RemarkableConnection {

    private final static Logger log = LoggerFactory.getLogger(RemarkableConnection.class);

    private final SSHClient sshClient;
    private SFTPClient sftpClient;

    public RemarkableConnection() throws IOException {

        sshClient = new SSHClient();
        sshClient.loadKnownHosts();
        sshClient.setConnectTimeout(200);
    }

    private void ensureConnection() throws RemarkableConnectionException {
        if (sshClient.isConnected() && sshClient.isAuthenticated()) {
            return;
        }

        try {
            sshClient.connect("192.168.2.2");
        } catch (IOException e) {
            throw new RemarkableConnectionException("Trouble connecting to Remarkable", e);
        }

        try {
            sshClient.authPassword("root", "ZjZQdup7xQ");
            connectSftp();
        } catch (UserAuthException e) {
            throw new RemarkableConnectionException("Failed to authenticate", e);
        } catch (TransportException e) {
            throw new RemarkableConnectionException("Trouble with connection during authentication", e);
        }
    }

    private void connectSftp() throws RemarkableConnectionException {
        if (sftpClient == null) {
            try {
                sftpClient = sshClient.newSFTPClient();
            } catch (IOException e) {
                throw new RemarkableConnectionException("Error creating sftp client", e);
            }
        }
    }

    List<RemoteResourceInfo> readFileTree() throws RemarkableConnectionException {
        ensureConnection();

        List<RemoteResourceInfo> listing;
        try {
            listing = sftpClient.ls(
                    "/home/root/.local/share/remarkable/xochitl",
                    resource -> resource.getName().endsWith("metadata"));
        } catch (IOException e) {
            throw new RemarkableConnectionException("Trouble listing files", e);
        }

//        // Should throw from this point, or just ignore / log?
//        try {
//            sftpClient.close();
//        } catch (IOException e) {
//            throw new ConnectionException("Error during closing of sftClient", e);
//        }

        return listing;
    }

    Optional<byte[]> readFile(Path path) throws RemarkableConnectionException {
        ensureConnection();

        RemoteFile file = null;
        try {
            file = sftpClient.open(path.toString());
            int fileLength = Math.toIntExact(file.length());
            byte[] contents = new byte[fileLength];
            file.read(0, contents, 0, fileLength);

            return Optional.of(contents);
        } catch (SFTPException e) {
            if (e.getStatusCode().equals(Response.StatusCode.NO_SUCH_FILE)) {
                return Optional.empty();
            } else {
                log.error("Error opening file: " + path, e);
                throw new RemarkableConnectionException("Error opening file: " + path, e);
            }
        } catch (IOException e) {
            log.error("Error reading file: " + path, e);
            throw new RemarkableConnectionException("Error reading file: " + path, e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    // No need to rethrow, because contents already read
                    log.error("Error closing file: " + path, e);
                }
            }
        }
    }

    String readFileOld(String path) throws RemarkableConnectionException {
        ensureConnection();

        RemoteFile file;
        try {
            file = sftpClient.open(path);
        } catch (IOException e) {
            throw new RemarkableConnectionException("Error opening file: " + path, e);
        }

        int length;
        try {
            length = (int) file.length();
        } catch (IOException e) {
            throw new RemarkableConnectionException("Error determining length of: " + file, e);
        }
        byte[] contents = new byte[length];
        try {
            file.read(0, contents, 0, length);
        } catch (IOException e) {
            throw new RemarkableConnectionException("Error reading file: " + file, e);
        }

        try {
            file.close();
        } catch (IOException e) {
            throw new RemarkableConnectionException("Error closing file: " + file, e);
        }

        return new String(contents, StandardCharsets.UTF_8);
    }

    void writeNewFile(String name, String content) throws RemarkableConnectionException {

    }

    void createDirectory(UUID id, String name) throws RemarkableConnectionException {
        SFTPClient sftpClient;
        try {
            sftpClient = sshClient.newSFTPClient();
        } catch (IOException e) {
            throw new RemarkableConnectionException("Trouble creating SFTP client", e);
        }

        RemoteFile file;
        try {
            file = sftpClient.open(id.toString(), Set.of(OpenMode.CREAT, OpenMode.EXCL,
                    OpenMode.WRITE));
        } catch (IOException e) {
            throw new RemarkableConnectionException("Error creating file (for directory)", e);
        }

//        file.write();
//        file.close();
    }
}