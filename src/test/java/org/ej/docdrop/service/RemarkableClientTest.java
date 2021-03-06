package org.ej.docdrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.assertj.core.util.Lists;
import org.ej.docdrop.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RemarkableClientTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final RemarkableMetadataBuilder metadataBuilder = new RemarkableMetadataBuilder()
            .setDeleted(false)
            .setLastModified(fixedClock.instant().truncatedTo(ChronoUnit.MILLIS))
            .setMetadataModified(false)
            .setModified(false)
            .setParent(null)
            .setPinned(false)
            .setType(DocumentType.FOLDER)
            .setVersion(1)
            .setVisibleName("folder name");

    @Nested
    @DisplayName("folderExists")
    class FolderExists {
        @Test
        @DisplayName("throws error when connection is busy")
        void throwWhenBusy() {
        }

        @Test
        @DisplayName("re-throws connection errors")
        void rethrowError() {
        }

        @Test
        @DisplayName("returns false when no file with given id present")
        void returnFalseWhenNoFileFound() throws RemarkableClientException, RemarkableConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);

            when(connection.readFile(any())).thenReturn(Optional.empty());

            // When
            boolean result = client.folderExists(UUID.randomUUID());

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when deleted flag is set")
        void returnFalseWhenDeleted() throws JsonProcessingException, RemarkableConnectionException,
                RemarkableClientException {
            RemarkableMetadata metadata = metadataBuilder.setDeleted(true).create();
            String metadataJson = mapper.writeValueAsString(metadata);

            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);

            UUID folderId = UUID.randomUUID();
            when(connection.readFile(any())).thenReturn(Optional.of(metadataJson.getBytes(StandardCharsets.UTF_8)));

            // When
            boolean result = client.folderExists(folderId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("throws error when file is not of type folder")
        void throwErrorWhenNotFolder() throws JsonProcessingException, RemarkableConnectionException {
            RemarkableMetadata metadata = metadataBuilder.setType(DocumentType.DOCUMENT).create();
            String metadataJson = mapper.writeValueAsString(metadata);

            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);

            UUID folderId = UUID.randomUUID();
            when(connection.readFile(any())).thenReturn(Optional.of(metadataJson.getBytes(StandardCharsets.UTF_8)));

            // When, Then
            assertThatThrownBy(() -> client.folderExists(folderId)).isInstanceOf(RemarkableClientException.class);
        }

        @Test
        @DisplayName("reads metadata to check if id represents a folder")
        void returnTrueWhenPresent() throws RemarkableConnectionException, RemarkableClientException,
                JsonProcessingException {
            // Given
            RemarkableMetadata metadata = metadataBuilder.create();
            String metadataJson = mapper.writeValueAsString(metadata);

            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);

            UUID folderId = UUID.randomUUID();
            when(connection.readFile(RemarkableClient.BASE_PATH.resolve(folderId + ".metadata"))).thenReturn(Optional.of(metadataJson.getBytes(StandardCharsets.UTF_8)));

            // When
            boolean result = client.folderExists(folderId);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("createFolder")
    class CreateFolder {

        @Test
        @DisplayName("creates content & metadata file under base path")
        void createContentAndMetadataFiles() throws IOException, RemarkableConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);
            UUID folderId = UUID.randomUUID();

            // When
            client.createFolder(folderId, "folder name", null);

            // Then
            ArgumentCaptor<Path> filenameCaptor = ArgumentCaptor.forClass(Path.class);
            ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(connection, times(2))
                    .writeNewFile(filenameCaptor.capture(), contentCaptor.capture());

            assertThat(filenameCaptor.getAllValues().get(0)).isEqualTo(RemarkableClient.BASE_PATH.resolve(folderId +
                    ".content"));
            assertThat(contentCaptor.getAllValues().get(0)).isEqualTo("{}".getBytes(StandardCharsets.UTF_8));

            assertThat(filenameCaptor.getAllValues().get(1)).isEqualTo(RemarkableClient.BASE_PATH.resolve(folderId +
                    ".metadata"));
            RemarkableMetadata metadata = mapper.readValue(contentCaptor.getAllValues().get(1),
                    RemarkableMetadata.class);

            RemarkableMetadata expectedMetadata = new RemarkableMetadataBuilder()
                    .setDeleted(false)
                    .setLastModified(fixedClock.instant().truncatedTo(ChronoUnit.MILLIS))
                    .setMetadataModified(false)
                    .setModified(false)
                    .setParent(null)
                    .setPinned(false)
                    .setType(DocumentType.FOLDER)
                    .setVersion(1)
                    .setVisibleName("folder name")
                    .create();

            assertThat(metadata).isEqualTo(expectedMetadata);
        }

        @Test
        @DisplayName("puts parent id in metadata")
        void putParentInMetadata() throws IOException, RemarkableConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);
            UUID folderId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();

            // When
            client.createFolder(folderId, "another name", parentId);

            // Then
            ArgumentCaptor<Path> filenameCaptor = ArgumentCaptor.forClass(Path.class);
            ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(connection, times(2))
                    .writeNewFile(filenameCaptor.capture(), contentCaptor.capture());
            RemarkableMetadata metadata = mapper.readValue(contentCaptor.getAllValues().get(1),
                    RemarkableMetadata.class);

            RemarkableMetadata expectedMetadata = new RemarkableMetadataBuilder()
                    .setDeleted(false)
                    .setLastModified(fixedClock.instant().truncatedTo(ChronoUnit.MILLIS))
                    .setMetadataModified(false)
                    .setModified(false)
                    .setParent(parentId)
                    .setPinned(false)
                    .setType(DocumentType.FOLDER)
                    .setVersion(1)
                    .setVisibleName("another name")
                    .create();

            assertThat(metadata).isEqualTo(expectedMetadata);
        }

        @Test
        @DisplayName("re-throws connection exceptions")
        void rethrowExceptions() throws RemarkableConnectionException {
            RemarkableConnection connection = mock(RemarkableConnection.class);
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);
            UUID folderId = UUID.randomUUID();

            doThrow(new RemarkableConnectionException("BOOM!", null)).when(connection).writeNewFile(any(), any());

            // When
            assertThatThrownBy(() -> client.createFolder(folderId, "another name", null))
                    .isInstanceOf(RemarkableConnectionException.class);
        }
    }

    @Nested
    @DisplayName("uploadFile")
    class UploadFile {

        @Test
        @DisplayName("Create metadata, content & 'real' file")
        void createFiles() throws IOException, RemarkableConnectionException {
            RemarkableConnection connection = mock(RemarkableConnection.class);
            Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
            RemarkableClient client = new RemarkableClient(connection, fixedClock, mapper);

            UUID fileId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            RemarkableMetadata expectedMetadata = new RemarkableMetadataBuilder()
                    .setDeleted(false)
                    .setLastModified(fixedClock.instant().truncatedTo(ChronoUnit.MILLIS))
                    .setMetadataModified(false)
                    .setModified(false)
                    .setParent(parentId)
                    .setPinned(false)
                    .setType(DocumentType.DOCUMENT)
                    .setVersion(1)
                    .setVisibleName("Name")
                    .create();
            RemarkableContent expectedContent = new RemarkableContent(
                    mapper.createObjectNode(),
                    RemarkableFileType.PDF,
                    "",
                    0,
                    -1,
                    100,
                    1,
                    1,
                    new RemarkableTransform(1, 1, 1, 1, 1, 1, 1, 1, 1));

            Path dummyPdf = Files.createTempFile("client-test", "");
            Files.writeString(dummyPdf, "dummy pdf content");

            // When
            client.uploadFile(fileId, parentId, "Name", dummyPdf, null);

            // Then
            ArgumentCaptor<Path> filenameCaptor = ArgumentCaptor.forClass(Path.class);
            ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);

            verify(connection, times(3)).writeNewFile(filenameCaptor.capture(), contentCaptor.capture());

            Path contentPath = filenameCaptor.getAllValues().get(0);
            Path metadataPath = filenameCaptor.getAllValues().get(1);
            Path pdfPath = filenameCaptor.getAllValues().get(2);

            RemarkableContent content = mapper.readValue(contentCaptor.getAllValues().get(0), RemarkableContent.class);
            RemarkableMetadata metadata = mapper.readValue(contentCaptor.getAllValues().get(1),
                    RemarkableMetadata.class);
            byte[] pdf = contentCaptor.getAllValues().get(2);

            assertThat(content).isEqualTo(expectedContent);
            assertThat(metadata).isEqualTo(expectedMetadata);
            assertThat(pdf).isEqualTo("dummy pdf content".getBytes(StandardCharsets.UTF_8));

            assertThat(contentPath).isEqualTo(RemarkableClient.BASE_PATH.resolve(fileId + ".content"));
            assertThat(metadataPath).isEqualTo(RemarkableClient.BASE_PATH.resolve(fileId + ".metadata"));
            assertThat(pdfPath).isEqualTo(RemarkableClient.BASE_PATH.resolve(fileId + ".pdf"));
        }

        @Test
        @DisplayName("rethrow connection exceptions")
        void rethrowExceptions() {

        }
    }

    @Nested
    class ReadingFileTree {

        @Test
        void returnConnectedWhenReadingFileTree() {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC(), mapper);

            // When
            RemarkableStatus status = client.readFileTree(null, null);

            // Then
            assertThat(status).isEqualTo(RemarkableStatus.AVAILABLE);
        }

        @Test
        void passFilesToConsumerFunction() throws RemarkableConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC(), mapper);

            UUID file1Id = UUID.randomUUID();
            UUID file2Id = UUID.randomUUID();
            RemoteResourceInfo file1 = new RemoteResourceInfo(
                    new PathComponents("", file1Id + ".metadata", "/"), null);
            RemoteResourceInfo file2 = new RemoteResourceInfo(
                    new PathComponents("", file2Id + ".metadata", "/"), null);
            when(connection.readFileTree()).thenReturn(List.of(file1, file2));
            when(connection.readFileOld(file1Id + ".metadata")).thenReturn("file1");
            when(connection.readFileOld(file2Id + ".metadata")).thenReturn("file2");

            // When
            ConcurrentHashMap<UUID, String> files = new ConcurrentHashMap<>();
            RemarkableStatus status = client.readFileTree(files::put, null);

            // Then
            assertThat(files).hasSize(2);
            assertThat(files).containsEntry(file1Id, "file1");
            assertThat(files).containsEntry(file2Id, "file2");
        }

        @Test
        void onlyAllowOneReadAtTheTime() throws RemarkableConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC(), mapper);

            when(connection.readFileTree()).thenAnswer((invocation -> {
                Thread.sleep(700);
                return Lists.emptyList();
            }));

            // When
            RemarkableStatus first = client.readFileTree(null, null);
            RemarkableStatus second = client.readFileTree(null, null);

            // Then
            assertThat(first).isEqualTo(RemarkableStatus.AVAILABLE);
            assertThat(second).isEqualTo(RemarkableStatus.BUSY);
        }

        @Test
        void invokeErrorHandlingWhenConnectionCannotBeEstablished() throws RemarkableConnectionException,
                ExecutionException, InterruptedException, TimeoutException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC(), mapper);

            RemarkableConnectionException connectionException = new RemarkableConnectionException("AI", null);
            when(connection.readFileTree()).thenThrow(connectionException);

            // When
            CompletableFuture<RemarkableConnectionException> result = new CompletableFuture<>();
            client.readFileTree(null, result::complete);

            // Then
            assertThat(result.get(100, TimeUnit.MILLISECONDS)).isEqualTo(connectionException);
        }
    }
}