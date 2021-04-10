package org.ej.docdrop.service;

import org.ej.docdrop.domain.Document;
import org.ej.docdrop.repository.DocumentRepository;
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
    private final Path uploadPath;

    public DocumentService(@Value("${upload.path}") String uploadDirectory,
                           DocumentRepository documentRepository) {
        this.uploadPath = Paths.get(uploadDirectory);
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
}