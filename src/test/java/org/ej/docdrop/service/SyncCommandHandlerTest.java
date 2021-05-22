package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.SyncResult;
import org.ej.docdrop.domain.UploadFileCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SyncCommandHandlerTest {

    private SyncCommandHandler handler;
    private RemarkableClient client;
    private FileStorage storage;

    @BeforeEach
    void setupHandlerWithFreshMocks() {
        client = mock(RemarkableClient.class);
        storage = mock(FileStorage.class);

        handler = new SyncCommandHandler(client, storage);
    }

    @Nested
    @DisplayName("Create folder")
    class CreateFolder {

        @Test
        @DisplayName("fails when parent directory doesn't exist")
        void failWithoutParent() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(false);
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("fails when folder already exists")
        void failWhenFolderExists() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.folderExists(command.getFileId())).thenReturn(true);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("creates the folder when pre-conditions are met")
        void createFolder() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.SUCCESS);
            verify(client, times(1)).createFolder(command.getFileId(), command.getName(), command.getParentId());
        }

        @Test
        @DisplayName("creates the folder when pre-conditions are met (in root folder")
        void createFolderInRootFolder() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);

            when(client.folderExists(null)).thenThrow(new NullPointerException());
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.SUCCESS);
            verify(client, times(1)).createFolder(command.getFileId(), command.getName(), command.getParentId());
        }

        @Test
        @DisplayName("aborts when client connection is available")
        void abortWhenClientNotAvailable() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenThrow(new RemarkableConnectionException("Not connected", null));
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When, Then
            assertThatThrownBy(() ->handler.apply(command)).isInstanceOf(RemarkableConnectionException.class);
        }

        @Test
        @DisplayName("creates failure event when client throw error")
        void failWhenRemarkableClientFails() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenThrow(new RemarkableClientException("Ai"));
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.EXECUTION_FAILED);
            assertThat(event.getMessage()).isEqualTo("Ai");
        }
    }

    @Nested
    @DisplayName("Copy file to Remarkable")
    class CopyFile {

        @Test
        @DisplayName("fails when parent folder doesn't exists")
        void failWhenNoParent() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(false);
            when(client.fileExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("fails when file already exists")
        void failWhenAlreadyExists() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.fileExists(command.getFileId())).thenReturn(true);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("copies given file with the thumbnail")
        void copyFile() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());
            Path filePath = Path.of("file");
            Path thumbnailPath = Path.of("thumbnail");

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.fileExists(command.getFileId())).thenReturn(false);
            when(storage.getFilePath(command.getFileId())).thenReturn(filePath);
            when(storage.getThumbnailPath(command.getFileId())).thenReturn(thumbnailPath);

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.SUCCESS);
            verify(client, times(1)).uploadFile(command.getFileId(), command.getParentId(), "name", filePath, thumbnailPath);
        }

        @Test
        @DisplayName("aborts when client connection not available")
        void abortWhenConnectionNotAvailable() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenThrow(new RemarkableConnectionException("Not connected", null));
            when(client.fileExists(command.getFileId())).thenReturn(true);

            // When, Then
            assertThatThrownBy(() ->  handler.apply(command)).isInstanceOf(RemarkableConnectionException.class);
        }

        @Test
        @DisplayName("creates failure event when client throw error")
        void failWhenRemarkableClientFails() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.fileExists(command.getFileId())).thenThrow(new RemarkableClientException("oh oh"));

            // When
            SyncEvent event = handler.apply(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncResult.EXECUTION_FAILED);
            assertThat(event.getMessage()).isEqualTo("oh oh");
        }
    }
}
