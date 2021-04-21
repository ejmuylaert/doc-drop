package org.ej.docdrop.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

/**
 * Cached information about the document read from the Remarkable filesystem.
 * <p>
 * This document is stored in the database to make it easy to browse the contents of the
 * Remarkable tablet without needing to keep the complete filetree in memory and/or have the
 * tablet connected during browsing.
 */
@Entity
public class CachedDocumentInfo {

    @Id
    private final UUID id;
    private final UUID parentId;
    private final boolean isFolder;
    private final String name;
    private final boolean isRecent;

    // Required to make JPA happy ...
    public CachedDocumentInfo() {
        this.id = null;
        this.parentId = null;
        this.isFolder = false;
        this.name = null;
        this.isRecent = false;
    }

    public CachedDocumentInfo(UUID id, UUID parentId, boolean isFolder, String name) {
        this.id = id;
        this.parentId = parentId;
        this.isFolder = isFolder;
        this.name = name;
        this.isRecent = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedDocumentInfo that = (CachedDocumentInfo) o;
        return isFolder == that.isFolder && Objects.equals(id, that.id) && Objects.equals(parentId, that.parentId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, isFolder, name);
    }

    @Override
    public String toString() {
        return "CachedDocumentInfo{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", isFolder=" + isFolder +
                ", name='" + name + '\'' +
                '}';
    }
}