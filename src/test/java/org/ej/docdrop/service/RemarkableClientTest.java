package org.ej.docdrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.assertj.core.util.Lists;
import org.ej.docdrop.domain.DocumentType;
import org.ej.docdrop.domain.RemarkableMetadata;
import org.ej.docdrop.domain.RemarkableMetadataBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RemarkableClientTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void CreateFolderCreatesTwoFiles() throws ConnectionException, JsonProcessingException {
        // Given
        RemarkableConnection connection = mock(RemarkableConnection.class);
        Clock fixedClock = Clock.fixed(Instant.now(),
                ZoneId.systemDefault());
        RemarkableClient client = new RemarkableClient(connection, fixedClock);
        UUID folderId = UUID.randomUUID();

        // When
        client.createFolder(folderId, "folder name");

        // Then
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection, times(2)).writeNewFile(filenameCaptor.capture(),
                contentCaptor.capture());

        assertThat(filenameCaptor.getAllValues().get(0)).isEqualTo(folderId + ".content");
        assertThat(contentCaptor.getAllValues().get(0)).isEqualTo("{}");

        assertThat(filenameCaptor.getAllValues().get(1)).isEqualTo(folderId + ".metadata");
        RemarkableMetadata metadata = mapper.readValue(contentCaptor.getAllValues().get(1),
                RemarkableMetadata.class);
        RemarkableMetadataBuilder builder = new RemarkableMetadataBuilder();
        RemarkableMetadata expectedMetadata = builder
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

    @Nested
    class ReadingFileTree {

        @Test
        void returnConnectedWhenReadingFileTree() {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC());

            // When
            RemarkableStatus status = client.readFileTree(null, null);

            // Then
            assertThat(status).isEqualTo(RemarkableStatus.AVAILABLE);
        }

        @Test
        void passFilesToConsumerFunction() throws ConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC());

            UUID file1Id = UUID.randomUUID();
            UUID file2Id = UUID.randomUUID();
            RemoteResourceInfo file1 = new RemoteResourceInfo(
                    new PathComponents("", file1Id + ".metadata", "/"), null);
            RemoteResourceInfo file2 = new RemoteResourceInfo(
                    new PathComponents("", file2Id + ".metadata", "/"), null);
            when(connection.readFileTree()).thenReturn(List.of(file1, file2));
            when(connection.readFile(file1Id + ".metadata")).thenReturn("file1");
            when(connection.readFile(file2Id + ".metadata")).thenReturn("file2");

            // When
            ConcurrentHashMap<UUID, String> files = new ConcurrentHashMap<>();
            RemarkableStatus status = client.readFileTree(files::put, null);

            // Then
            assertThat(files).hasSize(2);
            assertThat(files).containsEntry(file1Id, "file1");
            assertThat(files).containsEntry(file2Id, "file2");
        }

        @Test
        void onlyAllowOneReadAtTheTime() throws ConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC());

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
        void invokeErrorHandlingWhenConnectionCannotBeEstablished() throws ConnectionException,
                ExecutionException, InterruptedException, TimeoutException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection, Clock.systemUTC());

            ConnectionException connectionException = new ConnectionException("AI", null);
            when(connection.readFileTree()).thenThrow(connectionException);

            // When
            CompletableFuture<ConnectionException> result = new CompletableFuture<>();
            client.readFileTree(null, result::complete);

            // Then
            assertThat(result.get(100, TimeUnit.MILLISECONDS)).isEqualTo(connectionException);
        }
    }
}