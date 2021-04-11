package org.ej.docdrop.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.ej.docdrop.serializers.InstantToStringEpochSerializer;
import org.ej.docdrop.serializers.StringEpochToInstantDeserializer;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RemarkableMetadata {

    private final boolean deleted;
    @JsonDeserialize(using = StringEpochToInstantDeserializer.class)
    @JsonSerialize(using = InstantToStringEpochSerializer.class)
    private final Instant lastModified;
    private final int lastOpenedPage;
    @JsonProperty("metadatamodified")
    private final boolean metadataModified;
    private final boolean modified;
    private final UUID parent;
    private final boolean pinned;
    private final boolean synced;
    private final DocumentType type;
    private final int version;
    private final String visibleName;

    @JsonCreator
    public RemarkableMetadata(@JsonProperty("deleted") boolean deleted,
                              @JsonProperty("lastModified") Instant lastModified,
                              @JsonProperty("lastOpenedPage") int lastOpenedPage,
                              @JsonProperty("metadatamodified") boolean metadataModified,
                              @JsonProperty("modified") boolean modified,
                              @JsonProperty("parent") UUID parent,
                              @JsonProperty("pinned") boolean pinned,
                              @JsonProperty("synced") boolean synced,
                              @JsonProperty("type") DocumentType type,
                              @JsonProperty("version") int version,
                              @JsonProperty("visibleName") String visibleName) {
        this.deleted = deleted;
        this.lastModified = lastModified;
        this.lastOpenedPage = lastOpenedPage;
        this.metadataModified = metadataModified;
        this.modified = modified;
        this.parent = parent;
        this.pinned = pinned;
        this.synced = synced;
        this.type = type;
        this.version = version;
        this.visibleName = visibleName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public int getLastOpenedPage() {
        return lastOpenedPage;
    }

    public boolean isMetadataModified() {
        return metadataModified;
    }

    public boolean isModified() {
        return modified;
    }

    public UUID getParent() {
        return parent;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isSynced() {
        return synced;
    }

    public DocumentType getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public String getVisibleName() {
        return visibleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemarkableMetadata that = (RemarkableMetadata) o;
        return deleted == that.deleted && lastOpenedPage == that.lastOpenedPage && metadataModified == that.metadataModified && modified == that.modified && pinned == that.pinned && synced == that.synced && version == that.version && Objects.equals(lastModified, that.lastModified) && Objects.equals(parent, that.parent) && Objects.equals(type, that.type) && Objects.equals(visibleName, that.visibleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deleted, lastModified, lastOpenedPage, metadataModified, modified,
                parent, pinned, synced, type, version, visibleName);
    }

    @Override
    public String toString() {
        return "RemarkableDocument{" +
                "deleted=" + deleted +
                ", lastModified=" + lastModified +
                ", lastOpenedPage=" + lastOpenedPage +
                ", metadataModified=" + metadataModified +
                ", modified=" + modified +
                ", parent=" + parent +
                ", pinned=" + pinned +
                ", synced=" + synced +
                ", type='" + type + '\'' +
                ", version=" + version +
                ", visibleName='" + visibleName + '\'' +
                '}';
    }
}