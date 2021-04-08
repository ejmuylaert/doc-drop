package org.ej.docdrop.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DocumentTest {

    @Test
    void nameReturnsOriginalWhenNameNotSet() {
        // Given
        Document document = new Document("/path", "origin", null);

        // When
        String name = document.getDisplayName();

        // Then
        assertThat(name).isEqualTo("origin");
    }

    @Test
    void displayNameReturnsNameWhenSet() {
        // Given
        Document document = new Document("/path", "origin", "given");

        // When
        String name = document.getDisplayName();

        // Then
        assertThat(name).isEqualTo("given");
    }
}