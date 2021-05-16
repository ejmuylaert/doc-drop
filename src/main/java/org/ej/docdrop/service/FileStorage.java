package org.ej.docdrop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * FileStorage is responsible for managing local copies of files.
 * <p>
 * It handles the naming scheme of files and the storage location on the filesystem.
 */
@Component
public class FileStorage {

    private final Path storageDirectory;

    /**
     * @param storageDirectory location on the local filesystem, relative to path where the application is started
     */
    public FileStorage(@Value("${storage.path}") String storageDirectory) {
        this.storageDirectory = Paths.get(storageDirectory);
    }

    /**
     * Moves the file given by source to a permanent location, so it can be retrieved by id later.
     * <p>
     * The file is moved instead of copied for efficiency reasons and to minimize needed cleanup at the client side.
     *
     * @param id     of the file, so it can be later retrieved
     * @param source path of the file to be stored
     */
    public void putFile(UUID id, Path source) {
        try {
            Files.move(source, createFilePath(id));
        } catch (IOException e) {
            throw new StorageException("Could not move file, id: " + id, e);
        }
    }

    /**
     * Moves the thumbnail file to a permanent location, so it can be retrieved by id later.
     * <p>
     * The file is moved instead of copied for efficiency reasons and to minimize needed cleanup at the client side.
     *
     * @param id     of the file associated with this thumbnail
     * @param source path of the thumbnail file to be stored
     */
    public void putThumbnail(UUID id, Path source) {
        try {
            Files.move(source, createThumbnailPath(id));
        } catch (IOException e) {
            throw new StorageException("Could not move thumbnail file, id: " + id, e);
        }
    }

    /**
     * Returns file path for the file with the given id.
     * <p>
     * It will throw a runtime exception (StorageException) when the file for the id doesn't exists.
     *
     * @param id of the file to be fetched
     * @return path of the requested file
     */
    public Path getFilePath(UUID id) {
        Path filePath = createFilePath(id);

        if (Files.exists(filePath)) {
            return filePath;
        } else {
            throw new StorageException("File doesn't exist, id: " + id);
        }
    }

    /**
     * Returns path the thumbnail for the given id.
     * <p>
     * It will throw a runtime exception (StorageException) when the thumbnail for the id doesn't exists.
     *
     * @param id of the thumbnail to be fetched
     * @return path to the thumbnail file
     */
    public Path getThumbnailPath(UUID id) {
        Path filePath = createThumbnailPath(id);

        if (Files.exists(filePath)) {
            return filePath;
        } else {
            throw new StorageException("Thumbnail file doesn't exist, id: " + id);
        }
    }

    private Path createFilePath(UUID id) {
        return storageDirectory.resolve(id.toString());
    }

    private Path createThumbnailPath(UUID id) {
        return storageDirectory.resolve(id + ".thumbnail");
    }
}

class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
