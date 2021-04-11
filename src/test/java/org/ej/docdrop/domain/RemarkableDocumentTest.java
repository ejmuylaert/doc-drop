package org.ej.docdrop.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RemarkableDocumentTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeDocument() throws JsonProcessingException {
        // Given
        Instant lastModified =
                LocalDateTime.of(2020, 5, 22, 9, 17, 35, 752 * 1000000).toInstant(ZoneOffset.UTC);
        RemarkableDocument expectedDocument = new RemarkableDocument(false, lastModified
                , 0, false, false, UUID.fromString("cb4b44c8-07fe-41b6-9fd9-34b930f982c8"), false
                , true, "DocumentType", 4, "Sketch");

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
        RemarkableDocument document = mapper.readValue(metadata, RemarkableDocument.class);

        // Then
        assertThat(document).isEqualTo(expectedDocument);
    }

    @Test
    void serializeDocument() throws JsonProcessingException {
        // Given
        Instant lastModified =
                LocalDateTime.of(2020, 5, 22, 9, 17, 35, 752 * 1000000).toInstant(ZoneOffset.UTC);
        RemarkableDocument document = new RemarkableDocument(false, lastModified
                , 0, false, false, UUID.fromString("cb4b44c8-07fe-41b6-9fd9-34b930f982c8"), false
                , true, "DocumentType", 4, "Sketch");

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
        String json = mapper.writeValueAsString(document);

        // Then
        assertThat(mapper.readTree(json)).isEqualTo(mapper.readTree(expected));
    }
}