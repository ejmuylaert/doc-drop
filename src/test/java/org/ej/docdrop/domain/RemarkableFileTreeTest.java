package org.ej.docdrop.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RemarkableFileTreeTest {
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
                    .setType("DocumentType")
                    .setVersion(4)
                    .setVisibleName("Sketch");

    @Test
    void listRootLevelDocumentsWhenAllParentsAreNull() {
        // Given
        RemarkableFileTree tree = new RemarkableFileTree();
        List<RemarkableDocument> fileList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            fileList.add(new RemarkableDocument(UUID.randomUUID(), metadataBuilder.create()));
        }
        fileList.forEach(tree::addDocument);

        // When
        List<RemarkableDocument> retrievedList = tree.documentsWithParent(null);

        // Then
        assertThat(retrievedList).hasSameElementsAs(fileList);
    }

    @Test
    void listRootLevelWhenTreeContainsDirectories() {
        // Given
        RemarkableFileTree tree = new RemarkableFileTree();
        List<RemarkableDocument> fileList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            fileList.add(new RemarkableDocument(UUID.randomUUID(), metadataBuilder.create()));
        }
        fileList.forEach(tree::addDocument);

        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            int parentIndex = rand.nextInt(fileList.size());
            RemarkableDocument parent = fileList.get(parentIndex);
            tree.addDocument(new RemarkableDocument(
                    UUID.randomUUID(),
                    metadataBuilder.setParent(parent.getUuid()).create()));
        }

        // When
        List<RemarkableDocument> retrievedList = tree.documentsWithParent(null);

        // Then
        assertThat(retrievedList).hasSameElementsAs(fileList);
    }
}