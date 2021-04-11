package org.ej.docdrop.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RemarkableFileTree {

    private final List<RemarkableDocument> fileList = new ArrayList<>();

    public void addDocument(RemarkableDocument document) {
        fileList.add(document);
    }

    public List<RemarkableDocument> documentsWithParent(UUID parentId) {
        return fileList.stream()
                .filter(doc -> doc.hasParent(parentId))
                .collect(Collectors.toList());
    }
}