package org.ej.docdrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ej.docdrop.domain.DocumentType;
import org.ej.docdrop.domain.RemarkableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * The RemarkableClient is responsible for coordinating interaction with the device over SSH.
 * <p>
 * It ensures that only <b>one command at the time</b>œ is being executed. All async functions have a callback which can
 * be used for status updates etc.
 */
@Component
public class RemarkableClient {

    private final static Logger log = LoggerFactory.getLogger(RemarkableClient.class);

    private final RemarkableConnection connection;
    private final Clock clock;
    private final ObjectMapper mapper;

    private final StampedLock connectionLock = new StampedLock();
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static final Path BASE_PATH = Path.of("/home/root/.local/share/remarkable/xochitl");

    public RemarkableClient(RemarkableConnection connection, Clock clock, ObjectMapper mapper) {

        this.connection = connection;
        this.clock = clock;
        this.mapper = mapper;
    }

    /**
     * Checks if the folder with given id exists on the device by checking the metadata file. Returns false if metadata
     * not found, or the folder is flagged as deleted in the metadata.
     *
     * @param id of the folder
     * @return true if the folder exits, false otherwise
     * @throws RemarkableConnectionException when connection with device failed, check can be re-run when connection is
     *                                       re-established
     * @throws RemarkableClientException     when parsing the metadata failed or the the requested file doesn't
     *                                       represent a folder. No use in repeating the request without changes on the
     *                                       device.
     */
    public boolean folderExists(UUID id) throws RemarkableConnectionException, RemarkableClientException {
        Path filePath = BASE_PATH.resolve(id.toString() + ".metadata");
        Optional<byte[]> contents = connection.readFile(filePath);

        if (contents.isEmpty()) {
            return false;
        }

        try {
            RemarkableMetadata metadata = mapper.readValue(contents.get(), RemarkableMetadata.class);
            if (!metadata.getType().equals(DocumentType.FOLDER)) {
                throw new RemarkableClientException("Request file does not represents a folder, id: " + id
                        + ", metadata: " + metadata);
            }

            return !metadata.isDeleted();
        } catch (IOException e) {
            throw new RemarkableClientException("Error while parsing json metadata for: " + id, e);
        }
    }

    /**
     * Creates the files representing a folder on the device.
     * <p>
     * It assumes that the caller already verified that the folder doesn't already exists on the device.
     *
     * @param id       of the folder
     * @param name     name
     * @param parentId id of the parent folder
     * @throws RemarkableConnectionException when connection with device failed, check can be re-run when connection is
     *                                       re-established
     */
    void createFolder(UUID id, String name, UUID parentId) throws RemarkableConnectionException {
        // TODO: abstract get connection
        connection.writeNewFile(BASE_PATH.resolve(id + ".content").toString(), "{}");

        RemarkableMetadata metadata = new RemarkableMetadata(false, clock.instant(), 0, false, false, parentId, false,
                false, DocumentType.FOLDER, 1, name);

        try {
            String s = mapper.writeValueAsString(metadata);
            connection.writeNewFile(BASE_PATH.resolve(id + ".metadata").toString(), s);
        } catch (JsonProcessingException e) {
            // When this happen, there is a programming error. The RemarkableMetadata should always be serializable
            throw new RuntimeException("Error serializing metadata", e);
        }
    }


    public boolean fileExists(UUID id) throws RemarkableClientException {
        return false;
    }

    /**
     * Reads all metadata from the "/home/root/.local/share/remarkable/xochitl" folder on the Remarkable device, so an
     * overview of documents can be generated.
     * <p>
     * If there is already a command in progress, this method doesn't do anything (no queuing of commands).
     *
     * @param fileConsumer invoked with the fileId and metadata for each file
     * @param errorHandler invoked in case there is an underlying SSH error
     * @return BUSY when command is already being executed, else AVAILABLE
     */
    RemarkableStatus readFileTree(BiConsumer<UUID, String> fileConsumer,
                                  Consumer<RemarkableConnectionException> errorHandler) {

        final long lockStamp = connectionLock.tryWriteLock();
        if (lockStamp == 0) {
            return RemarkableStatus.BUSY;
        }

        executorService.submit(() -> {
            try {
                connection.readFileTree().forEach(info -> {
                    UUID id = UUID.fromString(baseName(info.getName()));
                    try {
                        String contents = connection.readFileOld(info.getPath());
                        fileConsumer.accept(id, contents);
                    } catch (RemarkableConnectionException e) {
                        e.printStackTrace();
                    }
                });
            } catch (RemarkableConnectionException e) {
                log.error("Error during readFileTree", e);
                errorHandler.accept(e);
            } finally {
                connectionLock.unlock(lockStamp);
            }

            errorHandler.accept(null);
        });

        return RemarkableStatus.AVAILABLE;
    }


    private static String baseName(String fileName) {
        int i = fileName.lastIndexOf('.');

        if (i == -1) {
            return fileName;
        } else {
            return fileName.substring(0, i);
        }
    }

    public void uploadFile(UUID fileId, UUID parentId, String name, Path path, Path thumbnailPath) throws RemarkableClientException {

    }
}

/**
 * Exception thrown in case of a non-recoverable error in RemarkableClient.
 * <p>
 * Retrying the method which throws this exception won't succeed when there is no change on, for example the Remarkable
 * tablet. (e.g. this is thrown when the metadata of a file cannot be parsed).
 */
class RemarkableClientException extends Throwable {
    public RemarkableClientException(String message) {
        super(message);
    }

    public RemarkableClientException(String message, Throwable cause) {
        super(message, cause);
    }
}