package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.*;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Import(FileService.class)
class FileServiceTest extends AbstractDatabaseTest {

    private final FileInfoRepository fileInfoRepository;
    private final SyncCommandRepository commandRepository;
    private final FileService service;

    @TempDir
    static Path storagePath;
    private Path uploadPath;

    public FileServiceTest(@Autowired FileInfoRepository fileInfoRepository,
                           @Autowired SyncCommandRepository commandRepository) {

        this.fileInfoRepository = mock(FileInfoRepository.class, delegatesTo(fileInfoRepository));
        this.commandRepository = mock(SyncCommandRepository.class,
                delegatesTo(commandRepository));

        this.service = new FileService(fileInfoRepository, commandRepository,
                storagePath.toString());
    }

    @BeforeEach
    void setUploadPath(@TempDir Path uploadPath) {
        this.uploadPath = uploadPath;
    }

    @Nested
    @DisplayName("Get folder path")
    class FolderPath {

        @Test
        @DisplayName("is empty on root folder")
        void rootFolder() {
            // When
            List<FileInfo> path = service.folderPath(null);

            // Then
            assertThat(path).isEmpty();
        }

        @Test
        @DisplayName("only the folder, if that folder is within root")
        void justTheFolder() {
            // Given
            FileInfo folder = new FileInfo(null, true, "name");
            fileInfoRepository.save(folder);

            // When
            List<FileInfo> path = service.folderPath(folder.getId());

            // Then
            assertThat(path).hasSize(1);
            assertThat(path.get(0).getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("list of folders, starting with the top one")
        void listOfFolders() {
            // Given
            FileInfo inRoot = new FileInfo(null, true, "in root");
            FileInfo nested = new FileInfo(inRoot.getId(), true, "nested");
            FileInfo nestedNested = new FileInfo(nested.getId(), true, "deeply nested");
            fileInfoRepository.save(inRoot);
            fileInfoRepository.save(nested);
            fileInfoRepository.save(nestedNested);

            // When
            List<FileInfo> path = service.folderPath(nestedNested.getId());

            // Then
            assertThat(path).hasSize(3);
            List<String> names = path.stream().map(FileInfo::getName).toList();
            assertThat(names).containsExactly("in root", "nested", "deeply nested");
        }
    }

    @Nested
    @DisplayName("Creating folder")
    class CreateFolder {

        @Test
        @DisplayName("stores new FileInfo entry in db")
        void storesNewFile() {
            // When
            FileInfo folder = service.createFolder("name", null);
            List<FileInfo> folderContents = service.folder(null);

            // Then
            assertThat(folderContents).hasSize(1);
            assertThat(folderContents.get(0)).isEqualTo(folder);
        }

        @Test
        @DisplayName("generates CreateFolderCommand")
        void generateCreateFolderCommand() {
            // When
            FileInfo folder = service.createFolder("new folder", null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(commands).hasSize(1);

            CreateFolderCommand command = (CreateFolderCommand) commands.iterator().next();
            assertThat(command.getFileId()).isEqualTo(folder.getId());
            assertThat(command.getCommandNumber()).isEqualTo(0);
            assertThat(command.getName()).isEqualTo("new folder");
        }

        @Test
        @DisplayName("when two folders are generated, commands have number 0 & 1")
        void generateNewCommandNumber() {
            // When
            service.createFolder("first", null);
            service.createFolder("second", null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            Iterator<SyncCommand> commandIterator = commands.iterator();
            CreateFolderCommand first = (CreateFolderCommand) commandIterator.next();
            CreateFolderCommand second = (CreateFolderCommand) commandIterator.next();

            assertThat(first.getCommandNumber()).isEqualTo(0);
            assertThat(first.getName()).isEqualTo("first");

            assertThat(second.getCommandNumber()).isEqualTo(1);
            assertThat(second.getName()).isEqualTo("second");
        }


        @Test
        @DisplayName("stores correct parentId in FileInfo & Command")
        void storeParentId() {
            // Given
            FileInfo folder = new FileInfo(null, true, "my folder");
            fileInfoRepository.save(folder);

            // When
            FileInfo newFolder = service.createFolder("nested", folder.getId());
            List<FileInfo> contents = service.folder(folder.getId());
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(newFolder.getParentId()).isEqualTo(folder.getId());
            assertThat(contents).hasSize(1);
            assertThat(commands).hasSize(1);

            CreateFolderCommand command = (CreateFolderCommand) commands.iterator().next();
            assertThat(command.getName()).isEqualTo("nested");
            assertThat(command.getParentId()).isEqualTo(folder.getId());
        }

        @Test
        @DisplayName("fails if parentId doesn't exist")
        void failIfParentDoesNotExist() {
            // When, Then
            assertThatThrownBy(() -> service.createFolder("child", UUID.randomUUID()))
                    .isInstanceOf(RuntimeException.class);

            // Then
            List<FileInfo> folder = service.folder(null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            assertThat(folder).hasSize(0);
            assertThat(commands).hasSize(0);
        }

        @Test
        @DisplayName("fails if parentId is not a folder")
        void failIfParentIsNoFolder() {
            // Given
            FileInfo file = new FileInfo(null, false, "my file");
            fileInfoRepository.save(file);

            // When, Then
            assertThatThrownBy(() -> service.createFolder("child", file.getId()))
                    .isInstanceOf(RuntimeException.class);

            Iterable<SyncCommand> commands = service.pendingCommands();
            assertThat(commands).hasSize(0);

            Iterable<FileInfo> allFiles = fileInfoRepository.findAll();
            assertThat(allFiles).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Store file")
    class StoreFile {

        @Test
        @DisplayName("Move uploaded file to permanent location")
        void moveFile() throws IOException {
            // Given
            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");
            Path thumbnail = uploadPath.resolve("thumbnail");
            Files.writeString(thumbnail, "unit test file contents ...");

            // When
            service.addFile("the name", testFile, thumbnail, null);

            // Then
            FileInfo info = service.folder(null).get(0);
            Path document = storagePath.resolve(info.getId().toString());

            assertThat(Files.exists(document)).isTrue();
            assertThat(document).isNotEqualTo(testFile);
            assertThat(Files.readString(document)).isEqualTo("unit test file contents ...");
        }

        @Test
        @DisplayName("Move thumbnail to permanent location")
        void moveThumbnail() throws IOException {
            // Given
            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");
            Path thumbnail = uploadPath.resolve("thumbnail");
            Files.writeString(thumbnail, "unit test file thumbnail ...");

            // When
            service.addFile("the name", testFile, thumbnail, null);

            // Then
            FileInfo info = service.folder(null).get(0);
            Path thumb = storagePath.resolve(info.getId() + ".thumbnail");

            assertThat(Files.exists(thumb)).isTrue();
            assertThat(thumb).isNotEqualTo(testFile);
            assertThat(Files.readString(thumb)).isEqualTo("unit test file thumbnail ...");
        }

        @Test
        @DisplayName("Create command for uploading file")
        void createCommand() throws IOException {
            // Given
            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");
            Path thumbnail = uploadPath.resolve("thumbnail");
            Files.writeString(thumbnail, "unit test file contents ...");

            // When
            service.addFile("the file name", testFile, thumbnail, null);
            List<FileInfo> contents = service.folder(null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(contents).hasSize(1);
            assertThat(commands).hasSize(1);

            UploadFileCommand command = (UploadFileCommand) commands.iterator().next();
            assertThat(command.getName()).isEqualTo("the file name");
            assertThat(command.getParentId()).isNull();
        }

        @Test
        @DisplayName("Increases command number on next event")
        void increaseCommandNumber() throws IOException {
            // Given
            Path testFile1 = uploadPath.resolve("my-ut-test-file-1");
            Files.writeString(testFile1, "unit test file contents ...");
            Path testFile2 = uploadPath.resolve("my-ut-test-file-2");
            Files.writeString(testFile2, "unit test file contents ...");
            Path thumbnail1 = uploadPath.resolve("thumbnail-1");
            Files.writeString(thumbnail1, "unit test file contents ...");
            Path thumbnail2 = uploadPath.resolve("thumbnail-2");
            Files.writeString(thumbnail2, "unit test file contents ...");


            // When
            service.addFile("first file", testFile1, thumbnail1, null);
            service.addFile("second file", testFile2, thumbnail2, null);
            List<FileInfo> contents = service.folder(null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(contents).hasSize(2);
            assertThat(commands).hasSize(2);

            Iterator<SyncCommand> commandIterator = commands.iterator();
            assertThat(commandIterator.next().getCommandNumber()).isEqualTo(0);
            assertThat(commandIterator.next().getCommandNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Store file in selected folder")
        void storeFileInFolder() throws IOException {
            // Given
            FileInfo folder = service.createFolder("my folder", null);
            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");
            Path thumbnail = uploadPath.resolve("thumbnail");
            Files.writeString(thumbnail, "unit test file contents ...");

            // When
            service.addFile("the file name", testFile, thumbnail, folder.getId());
            List<FileInfo> contents = service.folder(folder.getId());
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(contents).hasSize(1);
            assertThat(commands).hasSize(2);

            Iterator<SyncCommand> commandIterator = commands.iterator();
            commandIterator.next(); // pop createFolderCommand from iterator
            UploadFileCommand command = (UploadFileCommand) commandIterator.next();
            assertThat(command.getName()).isEqualTo("the file name");
            assertThat(command.getParentId()).isEqualTo(folder.getId());
        }

        @Test
        @DisplayName("Throw error when file doesn't exist")
        void throwWhenFileDoesNotExist() {
            // Given
            Path testFile = uploadPath.resolve("my-ut-test-file");

            // When, Then
            assertThatThrownBy(() -> service.addFile("name", testFile, testFile, null));
        }

        @Test
        @DisplayName("Throw error when folder doesn't exist")
        void throwWhenFolderDoesNotExist() throws IOException {
            // Given
            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");
            Path thumbnail = uploadPath.resolve("thumbnail");
            Files.writeString(thumbnail, "unit test file contents ...");

            // When, Then
            assertThatThrownBy(() ->
                    service.addFile("the name", testFile, thumbnail, UUID.randomUUID())
            ).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Rename file")
    class RenameFile {

        @Test
        @DisplayName("Rename the file")
        void renameFile() {
            // Given
            FileInfo existingFile = new FileInfo(null, false, "first name");
            fileInfoRepository.save(existingFile);

            // When
            service.renameFile(existingFile.getId(), "new name");
            List<FileInfo> folderContents = service.folder(null);

            // Then
            assertThat(folderContents).hasSize(1);

            FileInfo info = folderContents.get(0);
            assertThat(info.getId()).isEqualTo(existingFile.getId());
            assertThat(info.getName()).isEqualTo("new name");
        }

        @Test
        @DisplayName("Create command when renaming")
        void createCommand() {
            // Given
            FileInfo existingFile = new FileInfo(null, false, "first name");
            fileInfoRepository.save(existingFile);

            // When
            service.renameFile(existingFile.getId(), "new name");
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(commands).hasSize(1);
            RenameCommand command = (RenameCommand) commands.iterator().next();
            assertThat(command.getNewName()).isEqualTo("new name");
        }

        @Test
        @DisplayName("Throw error when original file doesn't exist")
        void throwErrorWhenFileDoesNotExist() {
            // When
            assertThatThrownBy(() -> service.renameFile(UUID.randomUUID(), "not possible"))
                    .isInstanceOf(Throwable.class);

            // Then
            List<FileInfo> files = service.folder(null);
            Iterable<SyncCommand> commands = service.pendingCommands();

            assertThat(files).hasSize(0);
            assertThat(commands).hasSize(0);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @Test
        @DisplayName("Delete the file")
        void deleteFile() {
            // Given
            FileInfo existingFile = new FileInfo(null, false, "file");
            fileInfoRepository.save(existingFile);

            // When
            service.removeFile(existingFile.getId());
            List<FileInfo> files = service.folder(null);

            // Then
            assertThat(files).hasSize(0);
        }

        @Test
        @DisplayName("Deletes also creates command")
        void createCommand() {
            // Given
            FileInfo existingFile = new FileInfo(null, false, "file");
            fileInfoRepository.save(existingFile);

            // When
            service.removeFile(existingFile.getId());
            Iterable<SyncCommand> commands = service.pendingCommands();

            // Then
            assertThat(commands).hasSize(1);
            assertThat(commands.iterator().next()).isInstanceOf(DeleteCommand.class);
            assertThat(commands.iterator().next().getFileId()).isEqualTo(existingFile.getId());
        }

        @Test
        @DisplayName("Delete folder")
        void deleteFolder() {
            // Given
            FileInfo existingFolder = new FileInfo(null, true, "folder");
            fileInfoRepository.save(existingFolder);

            // When
            service.removeFile(existingFolder.getId());
            List<FileInfo> files = service.folder(null);

            // Then
            assertThat(files).hasSize(0);
        }

        @Test
        @DisplayName("Throws error when file doesn't exist")
        void throwWhenNotExist() {
            assertThatThrownBy(() -> service.removeFile(UUID.randomUUID()));
        }

        @Test
        @DisplayName("Throws error when folder is not empty")
        void throwWhenFolderNotEmpty() {
            // Given
            FileInfo existingFolder = new FileInfo(null, true, "folder");
            fileInfoRepository.save(existingFolder);
            FileInfo existingFile = new FileInfo(existingFolder.getId(), true, "file");
            fileInfoRepository.save(existingFile);

            // When
            assertThatThrownBy(() -> service.removeFile(existingFolder.getId()));
            List<FileInfo> files = service.folder(existingFolder.getId());

            // Then
            assertThat(files).hasSize(1);
        }
    }
}