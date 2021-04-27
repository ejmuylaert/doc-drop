package org.ej.docdrop.service;

import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

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

    public FileServiceTransactionTest(@Autowired FileInfoRepository fileInfoRepository,
                                      @Autowired RemarkableCommandRepository commandRepository,
                                      @Autowired FileService service) {

        this.fileInfoRepository = fileInfoRepository;
        this.commandRepository = commandRepository;
        this.service = service;
    }

    @Test
    @DisplayName("stores correct parentId in FileInfo & Command")
    void storeParentId() {
        // Given
        FileInfo folder = new FileInfo(null, true, "my folder");
        fileInfoRepository.save(folder);

        SpringBeanMockUtil.mockFieldOnBean(service, FileInfoRepository.class, fileInfoRepository);
        SpringBeanMockUtil.mockFieldOnBean(service, RemarkableCommandRepository.class,
                commandRepository);

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
    @DisplayName("when FileInfo saving fails, no command is created")
    void noCommandWhenNoFileInfo() {
        fileInfoRepository.deleteAll();
        commandRepository.deleteAll();

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
        fileInfoRepository.deleteAll();
        commandRepository.deleteAll();

        RemarkableCommandRepository mockCommandRepository = mock(RemarkableCommandRepository.class,
                delegatesTo(commandRepository));

        // Given
        doAnswer(invocation -> {
            throw new RuntimeException("no saving ...");
        })
                .when(mockCommandRepository)
                .save(any());

        SpringBeanMockUtil.mockFieldOnBean(service, FileInfoRepository.class, fileInfoRepository);
        SpringBeanMockUtil.mockFieldOnBean(service, RemarkableCommandRepository.class,
                mockCommandRepository);

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