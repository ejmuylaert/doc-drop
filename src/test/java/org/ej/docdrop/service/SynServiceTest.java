package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.UploadFileCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SynServiceTest {

    @Test
    @DisplayName("Invokes 'execute' and marks as done")
    void executeAndMarkAsDone() {


    }

    @Nested
    @DisplayName("Upload file")
    class UploadFile {

        @Test
        @DisplayName("copies file to Remarkable")
        void copyFile() {
            // Given
            RemarkableClient client = mock(RemarkableClient.class);
            SyncService service = new SyncService(null, client, "path");
            UploadFileCommand command = new UploadFileCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            // When
            SyncEvent event = service.execute(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncEvent.Result.SUCCESS);
            verify(client, times(1)).uploadFile(command.getFileId(),
                    command.getParentId(), command.getName(), Path.of("path", command.getFileId().toString()), Path.of("path", command.getFileId() + ".thumbnail"));
        }
    }

    @Nested
    @DisplayName("Apply create folder command")
    class CreateFolder {

        @Test
        @DisplayName("fails when parent folder does not exist")
        void failWhenParentDoesNotExist() {
            //Given
            RemarkableClient client = mock(RemarkableClient.class);
            SyncService service = new SyncService(null, client, "");

            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());
            when(client.folderExists(command.getParentId())).thenReturn(false);
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = service.execute(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncEvent.Result.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("fails when folder already exist")
        void failWhenFolderAlreadyExists() {
            //Given
            RemarkableClient client = mock(RemarkableClient.class);
            SyncService service = new SyncService(null, client, "");

            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());
            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.folderExists(command.getFileId())).thenReturn(true);

            // When
            SyncEvent event = service.execute(command);

            // Then
            assertThat(event.getResult()).isEqualTo(SyncEvent.Result.PRE_CONDITION_FAILED);
        }

        @Test
        @DisplayName("creates folder when pre-conditions met")
        void createTheFolder() {
            //Given
            RemarkableClient client = mock(RemarkableClient.class);
            SyncService service = new SyncService(null, client, "");

            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", UUID.randomUUID());

            when(client.folderExists(command.getParentId())).thenReturn(true);
            when(client.folderExists(command.getFileId())).thenReturn(false);

            // When
            SyncEvent event = service.execute(command);

            // Then
            assertThat(event.getFileId()).isEqualTo(command.getFileId());
            assertThat(event.getCommandNumber()).isEqualTo(1L);
            assertThat(event.getResult()).isEqualTo(SyncEvent.Result.SUCCESS);

            verify(client, times(1)).createFolder(command.getFileId(), command.getName());
        }

        @Test
        @DisplayName("aborts when client connection not available")
        void whenClientNotAvailable() {

        }

        @Test
        @DisplayName("create event when execution of create folder fails")
        void whenCreateFolderFails() {

        }
    }
}