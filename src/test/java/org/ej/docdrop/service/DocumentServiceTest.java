package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.Document;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.SyncCommandRepository;
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
    private final FileInfoRepository fileRepository;
    private final DocumentService service;

    @TempDir
    static Path storagePath;
    private Path uploadPath;

    public DocumentServiceTest(@Autowired DocumentRepository repository,
                               @Autowired FileInfoRepository fileRepository,
                               @Autowired SyncCommandRepository commandRepository) {
        this.repository = repository;
        this.fileRepository = fileRepository;
        this.service = new DocumentService(storagePath.getParent().toString(), fileRepository,
                commandRepository, repository);
    }

    @BeforeEach
    void setUp(@TempDir Path tempUpload) {
        this.uploadPath = tempUpload;
    }

    @Test
    void moveFileToPermanentStorageLocation() throws IOException {
        // Given, When
        Path testFile = uploadPath.resolve("my-ut-test-file");
        Files.writeString(testFile, "unit test file");
        service.store(testFile, "original filename", Optional.of("new name"));

        // Then
        Document document = repository.findAll().iterator().next();
        Path documentLocation = Paths.get(document.getFilepath());

        assertThat(Files.exists(testFile)).isFalse();
        // The filepath should be stored releative to the storagePath, but not include it
        assertThat(documentLocation.getParent()).isNotEqualTo(storagePath.getParent());
        assertThat(documentLocation).isNotEqualTo(testFile);

        // Check if the file exists
        Path storedFilePath = storagePath.getParent().resolve(documentLocation);
        assertThat(Files.exists(storedFilePath)).isTrue();
    }

    @Test
    void storeDocumentEntry() throws IOException {
        // Given
        Path testFile = uploadPath.resolve("store-doc-entry");
        Files.writeString(testFile, "unit test file");

        //  When
        service.store(testFile, "original filename", Optional.of("new name"));

        // Then
        Document document = repository.findAll().iterator().next();

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

        assertThat(document.getOriginalName()).isEqualTo("original filename");
        assertThat(document.getName()).isNull();
    }

    @Test
    void createdFolderWillBeReturnedByFileService() {
        // When
        service.createFolder("unit test folder", null);

        // Then
        Iterable<FileInfo> files = service.files(null);

        assertThat(files).hasSize(1);
        FileInfo fileInfo = files.iterator().next();
        assertThat(fileInfo.getName()).isEqualTo("unit test folder");
        assertThat(fileInfo.isFolder()).isTrue();
        assertThat(fileInfo.getParentId()).isNull();
    }

    @Test
    void creatingFolderWillAlsoCreateRemarkableCommand() {
        // When
        service.createFolder("unit test folder", null);

        // Then
        Iterable<SyncCommand> commands = service.getPendingCommands();

        assertThat(commands).hasSize(1);
        SyncCommand command = commands.iterator().next();
        assertThat(command).isInstanceOf(CreateFolderCommand.class);

        CreateFolderCommand createFolderCommand = (CreateFolderCommand) command;
        assertThat(createFolderCommand.getName()).isEqualTo("unit test folder");
    }
}