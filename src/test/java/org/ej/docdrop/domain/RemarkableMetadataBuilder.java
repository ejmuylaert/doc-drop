package org.ej.docdrop.domain;

import java.time.Instant;
import java.util.UUID;

public class RemarkableMetadataBuilder {
    private boolean deleted;
    private Instant lastModified;
    private int lastOpenedPage;
    private boolean metadataModified;
    private boolean modified;
    private UUID parent;
    private boolean pinned;
    private boolean synced;
    private String type;
    private int version;
    private String visibleName;

    public RemarkableMetadataBuilder setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public RemarkableMetadataBuilder setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public RemarkableMetadataBuilder setLastOpenedPage(int lastOpenedPage) {
        this.lastOpenedPage = lastOpenedPage;
        return this;
    }

    public RemarkableMetadataBuilder setMetadataModified(boolean metadataModified) {
        this.metadataModified = metadataModified;
        return this;
    }

    public RemarkableMetadataBuilder setModified(boolean modified) {
        this.modified = modified;
        return this;
    }

    public RemarkableMetadataBuilder setParent(UUID parent) {
        this.parent = parent;
        return this;
    }

    public RemarkableMetadataBuilder setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public RemarkableMetadataBuilder setSynced(boolean synced) {
        this.synced = synced;
        return this;
    }

    public RemarkableMetadataBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public RemarkableMetadataBuilder setVersion(int version) {
        this.version = version;
        return this;
    }

    public RemarkableMetadataBuilder setVisibleName(String visibleName) {
        this.visibleName = visibleName;
        return this;
    }

    public RemarkableMetadata create() {
        return new RemarkableMetadata(deleted, lastModified, lastOpenedPage, metadataModified,
                modified, parent, pinned, synced, type, version, visibleName);
    }
}