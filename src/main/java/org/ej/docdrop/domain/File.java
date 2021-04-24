package org.ej.docdrop.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class File {

    @Id
    private final UUID id;
    private final UUID parentId;
    private final boolean isFolder;
    private final String name;

    protected File() {
        this.id = null;
        this.parentId = null;
        this.isFolder = false;
        this.name = null;
    }

    public File(UUID id, UUID parentId, boolean isFolder, String name) {
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
}