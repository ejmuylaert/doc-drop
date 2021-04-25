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
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FileServiceTest extends AbstractDatabaseTest {

    private final FileInfoRepository fileInfoRepository;
    private final RemarkableCommandRepository commandRepository;
    private final FileService service;

    public FileServiceTest(@Autowired FileInfoRepository fileInfoRepository,
                           @Autowired RemarkableCommandRepository commandRepository) {

        this.fileInfoRepository = fileInfoRepository;
        this.commandRepository = commandRepository;

        this.service = new FileService(fileInfoRepository, commandRepository);
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
            // TOD: create proper equals for CreateFolderCommand
//            assertThat(commands.iterator().next()).isEqualTo(
//                    new CreateFolderCommand(folder.getId(), 0, "new folder"));
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
        @DisplayName("fails if parentId is not a folder")
        void failIfParentIsNoFolder() {
        }

        @Test
        @DisplayName("stores correct parentId in FileInfo & Command")
        void storeParentId() {
        }
    }

    @Nested
    @DisplayName("FileInfo <-> RemarkableCommand transactions")
    class Transactions {

        @Test
        @DisplayName("when FileInfo saving fails, no command is created")
        void noCommandWhenNoFileInfo() {
        }

        @Test
        @DisplayName("when command save fails, no FileInfo is created")
        void noFileInfoWhenNoCommand() {
        }

        @Test
        @DisplayName("serialize saving, so order remains")
        void retainOrdering() {
        }

        @Test
        @DisplayName("when createFolderFails, deleteFolder also fails")
        void checksAreDoneWithinTransaction() {
        }
    }
}