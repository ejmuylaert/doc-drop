package org.ej.docdrop.domain;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.UUID;

@Entity
public class RenameCommand extends SyncCommand {
    private final String newName;

    protected RenameCommand() {
        this.newName = null;
    }

    public RenameCommand(UUID fileId, long commandNumber, String newName) {
        super(fileId, commandNumber, Instant.now());
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }
}