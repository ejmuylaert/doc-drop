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

    public DocumentService(@Value("upload") String uploadDirectory,
                           DocumentRepository documentRepository) {
        System.out.println("UPLOAD: " + uploadDirectory);
        this.uploadPath = Paths.get(uploadDirectory);
        System.out.println("PATH: " + uploadPath);
        this.documentRepository = documentRepository;
    }

    public void store(Path file, String originalName, Optional<String> givenName) {
        Path targetFilename = uploadPath.resolve(UUID.randomUUID().toString());

        try {
            Files.move(file, targetFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = new Document(targetFilename.toString(), originalName,
                givenName.orElse(null));

        documentRepository.save(document);
    }
}