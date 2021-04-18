package org.ej.docdrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CachedDocumentInfo;
import org.ej.docdrop.domain.DocumentType;
import org.ej.docdrop.domain.RemarkableMetadataBuilder;
import org.ej.docdrop.repository.CachedDocumentInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
class RemarkableServiceTest extends AbstractDatabaseTest {

    @Autowired
    private CachedDocumentInfoRepository repository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RemarkableMetadataBuilder metadataBuilder =
            new RemarkableMetadataBuilder()
                    .setDeleted(false)
                    .setLastModified(Instant.now())
                    .setLastOpenedPage(0)
                    .setMetadataModified(false)
                    .setModified(false)
                    .setParent(null)
                    .setPinned(false)
                    .setSynced(true)
                    .setType(DocumentType.DOCUMENT)
                    .setVersion(4)
                    .setVisibleName("Sketch");

    @Test
    void refreshFileTreeTest() throws JsonProcessingException {
        // Given
        RemarkableClient client = mock(RemarkableClient.class);
        UUID file1Id = UUID.randomUUID();
        UUID file2Id = UUID.randomUUID();
        String file1Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-1").create());
        String file2Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-2").create());
        when(client.readFileTree(any(), any())).thenAnswer(invocation -> {
            BiConsumer<UUID, String> fileConsumer = invocation.getArgument(0, BiConsumer.class);
            fileConsumer.accept(file1Id, file1Contents);
            fileConsumer.accept(file2Id, file2Contents);

            return RemarkableStatus.AVAILABLE;
        });

        RemarkableService service = new RemarkableService(client, repository);

        // When
        service.refreshFileTree();

        // Then
        assertThat(repository.findAll()).hasSize(2);
        assertThat(repository.findById(file1Id)).contains(
                new CachedDocumentInfo(file1Id, null, false, "file-1"));
        assertThat(repository.findById(file2Id)).contains(
                new CachedDocumentInfo(file2Id, null, false, "file-2"));
    }

    @Test
    void retrieveRootFolder() throws JsonProcessingException {
        // Given
        RemarkableClient client = mock(RemarkableClient.class);
        UUID file1Id = UUID.randomUUID();
        UUID file2Id = UUID.randomUUID();
        String file1Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-1").setParent(file2Id).create());
        String file2Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-2").setParent(null).create());
        when(client.readFileTree(any(), any())).thenAnswer(invocation -> {
            BiConsumer<UUID, String> fileConsumer = invocation.getArgument(0, BiConsumer.class);
            fileConsumer.accept(file1Id, file1Contents);
            fileConsumer.accept(file2Id, file2Contents);

            return RemarkableStatus.AVAILABLE;
        });

        RemarkableService service = new RemarkableService(client, repository);
        service.refreshFileTree();

        // When
        List<CachedDocumentInfo> rootFolder = service.getFolder(null);

        // Then
        assertThat(rootFolder).hasSize(1);
        assertThat(rootFolder).contains(new CachedDocumentInfo(file2Id, null, false, "file-2"));
    }

    @Test
    void retrieveFolderContents() throws JsonProcessingException {
        // Given
        RemarkableClient client = mock(RemarkableClient.class);
        UUID file1Id = UUID.randomUUID();
        UUID file2Id = UUID.randomUUID();
        String file1Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-1").setParent(file2Id).create());
        String file2Contents =
                mapper.writeValueAsString(metadataBuilder.setVisibleName("file-2").setParent(null).create());
        when(client.readFileTree(any(), any())).thenAnswer(invocation -> {
            BiConsumer<UUID, String> fileConsumer = invocation.getArgument(0, BiConsumer.class);
            fileConsumer.accept(file1Id, file1Contents);
            fileConsumer.accept(file2Id, file2Contents);

            return RemarkableStatus.AVAILABLE;
        });

        RemarkableService service = new RemarkableService(client, repository);
        service.refreshFileTree();

        // When
        List<CachedDocumentInfo> rootFolder = service.getFolder(file2Id);

        // Then
        assertThat(rootFolder).hasSize(1);
        assertThat(rootFolder).contains(new CachedDocumentInfo(file1Id, file2Id, false, "file-1"));
    }
}