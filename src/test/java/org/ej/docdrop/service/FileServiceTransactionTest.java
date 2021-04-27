package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true"
})
class FileServiceTransactionTest extends AbstractDatabaseTest {

    private final FileInfoRepository fileInfoRepository;
    private final RemarkableCommandRepository commandRepository;
    private final FileService service;

    @TempDir
    static Path uploadPath;

    public FileServiceTransactionTest(@Autowired FileInfoRepository fileInfoRepository,
                                      @Autowired RemarkableCommandRepository commandRepository,
                                      @Autowired FileService service) {

        this.fileInfoRepository = fileInfoRepository;
        this.commandRepository = commandRepository;
        this.service = service;
    }

    @BeforeEach
    void cleanDatabase() {
        fileInfoRepository.deleteAll();
        commandRepository.deleteAll();
    }

    @Test
    @DisplayName("stores correct parentId in FileInfo & Command")
    void storeParentId() {
        // Given
        SpringBeanMockUtil.mockFieldOnBean(service, FileInfoRepository.class, fileInfoRepository);
        SpringBeanMockUtil.mockFieldOnBean(service, RemarkableCommandRepository.class,
                commandRepository);

        FileInfo folder = new FileInfo(null, true, "my folder");
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

    @Nested
    @DisplayName("Create folder transactional behaviour")
    class CreateFolder {
        @Test
        @DisplayName("when FileInfo saving fails, no command is created")
        void noCommandWhenNoFileInfo() {
            // Given
            setupFailingInfoSave();

            // When
            assertThatThrownBy(() -> service.createFolder("name", null))
                    .isInstanceOf(Throwable.class);

            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(0);
            assertThat(commands).hasSize(0);
        }

        @Test
        @DisplayName("when command save fails, no FileInfo is created")
        void noFileInfoWhenNoCommand() {
            // Given
            setupFailingCommandSave();

            // When
            assertThatThrownBy(() -> service.createFolder("throw command", null))
                    .isInstanceOf(Throwable.class);

            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(0);
            assertThat(commands).hasSize(0);
        }
    }

    @Nested
    @DisplayName("Add file transactional behaviour")
    class AddFile {

        @Test
        @DisplayName("when file saving fails, no command saved")
        void failingInfoSave() throws IOException {
            // Given
            setupFailingInfoSave();

            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");

            // When
            assertThatThrownBy(() -> service.addFile("the name", testFile, null))
                    .isInstanceOf(Throwable.class);
            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(0);
            assertThat(commands).hasSize(0);
        }

        @Test
        @DisplayName("when command saving fails, no file saved")
        void failingCommandSave() throws IOException {
            // Given
            setupFailingCommandSave();

            Path testFile = uploadPath.resolve("my-ut-test-file");
            Files.writeString(testFile, "unit test file contents ...");

            // When
            assertThatThrownBy(() -> service.addFile("the name", testFile, null))
                    .isInstanceOf(Throwable.class);
            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(0);
            assertThat(commands).hasSize(0);
        }
    }

    @Nested
    @DisplayName("Rename file transactional behaviour")
    class RenameFile {

        @Test
        @DisplayName("when file saving fails, no command saved")
        void failingInfoSave() {
            // Given
            FileInfo original = new FileInfo(null, false, "original");
            fileInfoRepository.save(original);

            setupFailingInfoSave();

            // When
            assertThatThrownBy(() -> service.renameFile(original.getId(), "name"))
                    .isInstanceOf(Throwable.class);
            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(1);
            assertThat(folderContents.get(0).getName()).isEqualTo("original");
            assertThat(commands).hasSize(0);
        }

        @Test
        @DisplayName("when command saving fails, no file is saved")
        void failingCommandSave() {
            // Given
            FileInfo original = new FileInfo(null, false, "original");
            fileInfoRepository.save(original);

            setupFailingCommandSave();

            // When
            assertThatThrownBy(() -> service.renameFile(original.getId(), "name"))
                    .isInstanceOf(Throwable.class);
            List<FileInfo> folderContents = service.folder(null);
            Iterable<RemarkableCommand> commands = service.pendingCommands();

            // Then
            assertThat(folderContents).hasSize(1);
            assertThat(folderContents.get(0).getName()).isEqualTo("original");
            assertThat(commands).hasSize(0);
        }
    }

    private void setupFailingInfoSave() {
        FileInfoRepository mockFileInfoRepository = mock(FileInfoRepository.class,
                delegatesTo(fileInfoRepository));

        doAnswer(invocation -> {
            throw new RuntimeException("no saving ...");
        })
                .when(mockFileInfoRepository)
                .save(any());

        SpringBeanMockUtil.mockFieldOnBean(service, FileInfoRepository.class,
                mockFileInfoRepository);
        SpringBeanMockUtil.mockFieldOnBean(service, RemarkableCommandRepository.class,
                commandRepository);
    }

    private void setupFailingCommandSave() {
        RemarkableCommandRepository mockCommandRepository = mock(RemarkableCommandRepository.class,
                delegatesTo(commandRepository));

        doAnswer(invocation -> {
            throw new RuntimeException("no saving ...");
        })
                .when(mockCommandRepository)
                .save(any());

        SpringBeanMockUtil.mockFieldOnBean(service, FileInfoRepository.class, fileInfoRepository);
        SpringBeanMockUtil.mockFieldOnBean(service, RemarkableCommandRepository.class,
                mockCommandRepository);
    }
}

@SuppressWarnings("unchecked")
class SpringBeanMockUtil {
    /**
     * If the given object is a proxy, set the return value as the object being proxied,
     * otherwise return the given
     * object.
     */
    private static <T> T unwrapProxy(T bean) {
        try {
            if (AopUtils.isAopProxy(bean) && bean instanceof Advised advised) {
                bean = (T) advised.getTargetSource().getTarget();
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Could not unwrap proxy!", e);
        }
    }

    public static <T> T mockFieldOnBean(Object beanToInjectMock, Class<T> classToMock, T mocked) {
        ReflectionTestUtils.setField(unwrapProxy(beanToInjectMock), null, mocked, classToMock);
        return mocked;
    }
}