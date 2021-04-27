package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

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
    private final RemarkableCommandRepository commandRepository;
    private final FileService service;

    public FileServiceTest(@Autowired FileInfoRepository fileInfoRepository,
                           @Autowired RemarkableCommandRepository commandRepository,
                           @Autowired FileService service) {

        this.fileInfoRepository = mock(FileInfoRepository.class, delegatesTo(fileInfoRepository));
        this.commandRepository = mock(RemarkableCommandRepository.class,
                delegatesTo(commandRepository));

        this.service = service;
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
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(commands).hasSize(1);

            CreateFolderCommand command = (CreateFolderCommand) commands.iterator().next();
            assertThat(command.getFileId()).isEqualTo(folder.getId());
            assertThat(command.getCommandNumber()).isEqualTo(0);
            assertThat(command.isApplied()).isFalse();
            assertThat(command.getName()).isEqualTo("new folder");
        }

        @Test
        @DisplayName("when two folders are generated, commands have number 0 & 1")
        void generateNewCommandNumber() {
            // When
            service.createFolder("first", null);
            service.createFolder("second", null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            Iterator<RemarkableCommand> commandIterator = commands.iterator();
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
            FileInfo folder = new FileInfo(UUID.randomUUID(), null, true, "my folder");
            fileInfoRepository.save(folder);

            // When
            FileInfo newFolder = service.createFolder("nested", folder.getId());
            List<FileInfo> contents = service.folder(folder.getId());
            Iterable<RemarkableCommand> commands = service.pendingCommands();

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
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            assertThat(folder).hasSize(0);
            assertThat(commands).hasSize(0);
        }

        @Test
        @DisplayName("fails if parentId is not a folder")
        void failIfParentIsNoFolder() {
            // Given
            FileInfo file = new FileInfo(UUID.randomUUID(), null, false, "my file");
            fileInfoRepository.save(file);

            // When, Then
            assertThatThrownBy(() -> service.createFolder("child", file.getId()))
                    .isInstanceOf(RuntimeException.class);

            Iterable<RemarkableCommand> commands = service.pendingCommands();
            assertThat(commands).hasSize(0);

            Iterable<FileInfo> allFiles = fileInfoRepository.findAll();
            assertThat(allFiles).hasSize(1);
        }
    }
}