package org.ej.docdrop.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "file")
public class FileInfo {

    @Id
    private final UUID id;
    private final UUID parentId;
    private final boolean isFolder;
    private final String name;

    protected FileInfo() {
        this.id = null;
        this.parentId = null;
        this.isFolder = false;
        this.name = null;
    }

    public FileInfo(UUID id, UUID parentId, boolean isFolder, String name) {
        this.id = id;
        this.parentId = parentId;
        this.isFolder = isFolder;
        this.name = name;
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
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", isFolder=" + isFolder +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return isFolder == fileInfo.isFolder && Objects.equals(id, fileInfo.id) && Objects.equals(parentId, fileInfo.parentId) && Objects.equals(name, fileInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, isFolder, name);
    }
}