package org.ej.docdrop.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RemarkableContentTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeDocument() throws JsonProcessingException {
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

        String contentJson = """
                {
                    "extraMetadata": {
                    },
                    "fileType": "pdf",
                    "fontName": "",
                    "lastOpenedPage": 0,
                    "lineHeight": -1,
                    "margins": 100,
                    "pageCount": 1,
                    "textScale": 1,
                    "transform": {
                        "m11": 1,
                        "m12": 1,
                        "m13": 1,
                        "m21": 1,
                        "m22": 1,
                        "m23": 1,
                        "m31": 1,
                        "m32": 1,
                        "m33": 1
                    }
                }
                """;

        // When
        RemarkableContent content = mapper.readValue(contentJson, RemarkableContent.class);

        // Then
        assertThat(content).isEqualTo(expectedContent);
    }

    @Test
    void serializeDocument() throws JsonProcessingException {
        RemarkableContent content = new RemarkableContent(
                mapper.createObjectNode(),
                RemarkableFileType.PDF,
                "",
                0,
                -1,
                100,
                1,
                1,
                new RemarkableTransform(1, 1, 1, 1, 1, 1, 1, 1, 1));
        
        String expected = """
                {
                    "extraMetadata": {
                    },
                    "fileType": "pdf",
                    "fontName": "",
                    "lastOpenedPage": 0,
                    "lineHeight": -1,
                    "margins": 100,
                    "pageCount": 1,
                    "textScale": 1,
                    "transform": {
                        "m11": 1,
                        "m12": 1,
                        "m13": 1,
                        "m21": 1,
                        "m22": 1,
                        "m23": 1,
                        "m31": 1,
                        "m32": 1,
                        "m33": 1
                    }
                }
                """;

        // When
        String json = mapper.writeValueAsString(content);

        // Then
        assertThat(mapper.readTree(json)).isEqualTo(mapper.readTree(expected));
    }
}
