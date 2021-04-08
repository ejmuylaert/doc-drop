package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.Document;
import org.ej.docdrop.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
class DocumentServiceTest extends AbstractDatabaseTest {

    private final DocumentRepository repository;
    private final DocumentService service;

    @TempDir
    static Path storagePath;
    private Path uploadPath;

    public DocumentServiceTest(@Autowired DocumentRepository repository) {
        this.repository = repository;
        this.service = new DocumentService(storagePath.getParent().toString(), repository);
    }

    @BeforeEach
    void setUp(@TempDir Path tempUpload) {
        this.uploadPath = tempUpload;
    }

    @Test
    void moveFileToPermanentStorageLocation() {
        // Given, When
        service.store(uploadPath, "original filename", Optional.of("new name"));

        // Then
        Document document = repository.findAll().iterator().next();
        Path documentLocation = Paths.get(document.getFilepath());

        assertThat(Files.exists(documentLocation)).isTrue();
        assertThat(Files.exists(uploadPath)).isFalse();

        assertThat(documentLocation).isNotEqualTo(uploadPath);
        assertThat(documentLocation.getParent()).isEqualTo(storagePath.getParent());
    }

    @Test
    void storeDocumentEntry() throws IOException {
        // Given
        Files.writeString(uploadPath, "unit test file");

        //  When
        service.store(uploadPath, "original filename", Optional.of("new name"));

        // Then
        Document document = repository.findAll().iterator().next();

        Path storedFilePath = Paths.get(document.getFilepath());
        assertThat(storedFilePath.getParent()).isEqualTo(storagePath.getParent());
        assertThat(document.getOriginalName()).isEqualTo("original filename");
        assertThat(document.getName()).isEqualTo("new name");
    }

    @Test
    void storeDocumentEntryWithoutGivenNameIfAbsent() throws IOException {
        // Given
        Path testFilePath = uploadPath.resolve("without-name-ut");
        Files.writeString(testFilePath, "unit test file");

        // When
        service.store(testFilePath, "original filename", Optional.empty());

        // Then
        Document document = repository.findAll().iterator().next();

        Path storedFilePath = Paths.get(document.getFilepath());
        assertThat(storedFilePath.getParent()).isEqualTo(storagePath.getParent());
        assertThat(document.getOriginalName()).isEqualTo("original filename");
        assertThat(document.getName()).isNull();
    }
}