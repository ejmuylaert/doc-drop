package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.Document;
import org.ej.docdrop.domain.File;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.repository.FileRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
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
    private final FileRepository fileRepository;
    private final RemarkableCommandRepository commandRepository;
    private final Path uploadPath;

    public DocumentService(@Value("${upload.path}") String uploadDirectory,
                           FileRepository fileRepository,
                           RemarkableCommandRepository commandRepository,
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
        File file = new File(UUID.randomUUID(), null, true, name);
        CreateFolderCommand command = new CreateFolderCommand(file.getId(), 0, name);

        commandRepository.save(command);

        fileRepository.save(file);
    }

    public Iterable<File> files(UUID parentId) {
        return fileRepository.findAll();
    }

    public Iterable<RemarkableCommand> getPendingCommands() {
        return commandRepository.findAll();
    }
}