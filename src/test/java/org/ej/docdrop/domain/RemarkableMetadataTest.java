package org.ej.docdrop.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RemarkableMetadataTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeDocument() throws JsonProcessingException {
        // Given
        Instant lastModified =
                LocalDateTime.of(2020, 5, 22, 9, 17, 35, 752 * 1000000).toInstant(ZoneOffset.UTC);
        RemarkableMetadata expectedData =
                new RemarkableMetadataBuilder()
                        .setDeleted(false)
                        .setLastModified(lastModified)
                        .setLastOpenedPage(0)
                        .setMetadataModified(false)
                        .setModified(false)
                        .setParent(UUID.fromString("cb4b44c8-07fe-41b6-9fd9-34b930f982c8"))
                        .setPinned(false)
                        .setSynced(true)
                        .setType("DocumentType")
                        .setVersion(4)
                        .setVisibleName("Sketch")
                        .create();

        String metadata = """
                {
                    "deleted": false,
                    "lastModified": "1590139055752",
                    "lastOpenedPage": 0,
                    "metadatamodified": false,
                    "modified": false,
                    "parent": "cb4b44c8-07fe-41b6-9fd9-34b930f982c8",
                    "pinned": false,
                    "synced": true,
                    "type": "DocumentType",
                    "version": 4,
                    "visibleName": "Sketch"
                }""";

        // When
        RemarkableMetadata data = mapper.readValue(metadata, RemarkableMetadata.class);

        // Then
        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void serializeDocument() throws JsonProcessingException {
        // Given
        Instant lastModified =
                LocalDateTime.of(2020, 5, 22, 9, 17, 35, 752 * 1000000).toInstant(ZoneOffset.UTC);
        RemarkableMetadata data =
                new RemarkableMetadataBuilder()
                        .setDeleted(false)
                        .setLastModified(lastModified)
                        .setLastOpenedPage(0)
                        .setMetadataModified(false)
                        .setModified(false)
                        .setParent(UUID.fromString("cb4b44c8-07fe-41b6-9fd9-34b930f982c8"))
                        .setPinned(false)
                        .setSynced(true)
                        .setType("DocumentType")
                        .setVersion(4)
                        .setVisibleName("Sketch")
                        .create();

        String expected = """
                {
                    "deleted": false,
                    "lastModified": "1590139055752",
                    "lastOpenedPage": 0,
                    "metadatamodified": false,
                    "modified": false,
                    "parent": "cb4b44c8-07fe-41b6-9fd9-34b930f982c8",
                    "pinned": false,
                    "synced": true,
                    "type": "DocumentType",
                    "version": 4,
                    "visibleName": "Sketch"
                }""";

        // When
        String json = mapper.writeValueAsString(data);

        // Then
        assertThat(mapper.readTree(json)).isEqualTo(mapper.readTree(expected));
    }
}