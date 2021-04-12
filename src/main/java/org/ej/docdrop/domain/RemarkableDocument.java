package org.ej.docdrop.domain;

import java.util.Objects;
import java.util.UUID;

public class RemarkableDocument {
    private final UUID uuid;
    private final RemarkableMetadata metadata;

    public RemarkableDocument(UUID uuid, RemarkableMetadata metadata) {

        this.uuid = uuid;
        this.metadata = metadata;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return metadata.getVisibleName();
    }

    public boolean hasParent(UUID parentId) {
        return Objects.equals(metadata.getParent(), parentId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemarkableDocument that = (RemarkableDocument) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, metadata);
    }

    @Override
    public String toString() {
        return "RemarkableDocument{" +
                "uuid=" + uuid +
                ", metadata=" + metadata +
                '}';
    }
}