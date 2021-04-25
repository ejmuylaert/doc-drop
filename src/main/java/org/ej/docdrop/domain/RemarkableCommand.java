package org.ej.docdrop.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@IdClass(RemarkableCommand.CommandId.class)
public abstract class RemarkableCommand {

    @Id
    private final UUID fileId;
    @Id
    private final long commandNumber;

    private final boolean applied;

    protected RemarkableCommand() {
        this.fileId = null;
        this.commandNumber = 1;
        this.applied = false;
    }

    public RemarkableCommand(UUID fileId, long commandNumber) {
        this.fileId = fileId;
        this.commandNumber = commandNumber;
        this.applied = false;
    }

    public UUID getFileId() {
        return fileId;
    }

    public long getCommandNumber() {
        return commandNumber;
    }

    public boolean isApplied() {
        return applied;
    }

    public static class CommandId implements Serializable {
        private final UUID fileId;
        private final long commandNumber;

        protected CommandId() {
            this.fileId = null;
            this.commandNumber = -1;
        }

        public CommandId(UUID fileId, long commandNumber) {
            this.fileId = fileId;
            this.commandNumber = commandNumber;
        }
    }
}