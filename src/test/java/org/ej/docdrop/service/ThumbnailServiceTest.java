package org.ej.docdrop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ThumbnailServiceTest {

    private final ThumbnailService service = new ThumbnailService();

    @Test
    @DisplayName("Create thumbnail from PDF")
    void create() throws ThumbnailException, IOException {
        Path pdfPath = Paths.get("src", "test", "resources", "sample.pdf");

        // When
        Path thumbnail = service.createThumbnail(pdfPath);

        // Then
        assertThat(Files.exists(thumbnail)).isTrue();
    }
}