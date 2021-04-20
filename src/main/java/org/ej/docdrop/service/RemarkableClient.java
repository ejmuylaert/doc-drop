package org.ej.docdrop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * The RemarkableClient is responsible for coordinating interaction with the device over SSH.
 * <p>
 * It ensures that only <b>one command at the time</b>œ is being executed. All async functions
 * have a callback which can be used for status updates etc.
 */
@Component
public class RemarkableClient {

    private final static Logger log = LoggerFactory.getLogger(RemarkableClient.class);

    private final RemarkableConnection connection;

    private final StampedLock connectionLock = new StampedLock();
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public RemarkableClient(RemarkableConnection connection) {

        this.connection = connection;
    }

    /**
     * Reads all metadata from the "/home/root/.local/share/remarkable/xochitl" folder on the
     * Remarkable device, so an overview of documents can be generated.
     * <p>
     * If there is already a command in progress, this method doesn't do anything (no queuing of
     * commands).
     *
     * @param fileConsumer invoked with the fileId and metadata for each file
     * @param errorHandler invoked in case there is an underlying SSH error
     * @return BUSY when command is already being executed, else AVAILABLE
     */
    RemarkableStatus readFileTree(BiConsumer<UUID, String> fileConsumer,
                                  Consumer<ConnectionException> errorHandler) {

        final long lockStamp = connectionLock.tryWriteLock();
        if (lockStamp == 0) {
            return RemarkableStatus.BUSY;
        }

        executorService.submit(() -> {
            try {
                connection.readFileTree().forEach(info -> {
                    UUID id = UUID.fromString(baseName(info.getName()));
                    try {
                        String contents = connection.readFile(info.getPath());
                        fileConsumer.accept(id, contents);
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ConnectionException e) {
                log.error("Error during readFileTree", e);
                errorHandler.accept(e);
            } finally {
                connectionLock.unlock(lockStamp);
            }
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
}