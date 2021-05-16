package org.ej.docdrop.domain;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class CreateFolderCommand extends RemarkableCommand {

    private final String name;
    private final UUID parentId;

    protected CreateFolderCommand() {
        this.name = null;
        this.parentId = null;
    }

    public CreateFolderCommand(UUID fileId, long commandNumber, String name, UUID parentId) {
        super(fileId, commandNumber);

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