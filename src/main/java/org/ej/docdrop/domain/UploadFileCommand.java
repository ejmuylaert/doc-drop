package org.ej.docdrop.domain;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.UUID;

@Entity
public class UploadFileCommand extends SyncCommand {

    private final String name;
    private final UUID parentId;

    protected UploadFileCommand() {
        this.name = null;
        this.parentId = null;
    }

    public UploadFileCommand(UUID fileId, long commandNumber, String name, UUID parentId) {
        super(fileId, commandNumber, Instant.now());
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public UUID getParentId() {
        return parentId;
    }
}