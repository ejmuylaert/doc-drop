package org.ej.docdrop.domain;

import javax.persistence.Entity;
import java.time.Instant;
import java.util.UUID;

@Entity
public class DeleteCommand extends SyncCommand {

    protected DeleteCommand() {
    }

    public DeleteCommand(UUID fileId, long commandNumber) {
        super(fileId, commandNumber, Instant.now());
    }
}