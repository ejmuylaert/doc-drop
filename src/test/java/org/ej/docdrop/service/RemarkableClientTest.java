package org.ej.docdrop.service;

import net.schmizz.sshj.sftp.PathComponents;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemarkableClientTest {

    @Nested
    class ReadingFileTree {

        @Test
        void returnConnectedWhenReadingFileTree() {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection);

            // When
            RemarkableStatus status = client.readFileTree(null, null);

            // Then
            assertThat(status).isEqualTo(RemarkableStatus.AVAILABLE);
        }

        @Test
        void passFilesToConsumerFunction() throws ConnectionException {
            // Given
            RemarkableConnection connection = mock(RemarkableConnection.class);
            RemarkableClient client = new RemarkableClient(connection);

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
            RemarkableClient client = new RemarkableClient(connection);

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
            RemarkableClient client = new RemarkableClient(connection);

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