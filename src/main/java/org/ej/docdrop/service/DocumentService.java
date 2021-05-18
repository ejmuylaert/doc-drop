package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.Document;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileInfoRepository fileRepository;
    private final SyncCommandRepository commandRepository;
    private final Path uploadPath;

    public DocumentService(@Value("${upload.path}") String uploadDirectory,
                           FileInfoRepository fileRepository,
                           SyncCommandRepository commandRepository,
                           DocumentRepository documentRepository) {
        this.uploadPath = Paths.get(uploadDirectory);
        this.fileRepository = fileRepository;
        this.commandRepository = commandRepository;
        this.documentRepository = documentRepository;
    }

    public void store(Path file, String originalName, Optional<String> givenName) {
        String filename = UUID.randomUUID().toString();
        Path targetFilePath = uploadPath.resolve(filename);

        try {
            Files.move(file, targetFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = new Document(filename, originalName, givenName.orElse(null));

        documentRepository.save(document);
    }

    /**
     * Creates a folder in the database and a RemarkableCommand to create the folder on the
     * Remarkable when it is connected.
     *
     * @param name     the name of the folder
     * @param parentId id of the parent folder, or null when folder should be created in root folder
     */
    public void createFolder(String name, UUID parentId) {
        FileInfo fileInfo = new FileInfo(null, true, name);
        CreateFolderCommand command = new CreateFolderCommand(fileInfo.getId(), 0, name, null);

        commandRepository.save(command);

        fileRepository.save(fileInfo);
    }

    public Iterable<FileInfo> files(UUID parentId) {
        return fileRepository.findAll();
    }

    public Iterable<SyncCommand> getPendingCommands() {
        return commandRepository.findAll();
    }
}