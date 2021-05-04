package org.ej.docdrop.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@IdClass(RemarkableCommand.CommandId.class)
public abstract class RemarkableCommand {

    @Id
    private final UUID fileId;
    @Id
    private final long commandNumber;

    private Instant executionStartedAt;
    private Instant executedAt;

    protected RemarkableCommand() {
        this.fileId = null;
        this.commandNumber = 1;
    }

    public RemarkableCommand(UUID fileId, long commandNumber) {
        this.fileId = fileId;
        this.commandNumber = commandNumber;
    }

    public UUID getFileId() {
        return fileId;
    }

    public long getCommandNumber() {
        return commandNumber;
    }

    public Instant getExecutionStartedAt() {
        return executionStartedAt;
    }

    public void setExecutionStartedAt(Instant executionStartedAt) {
        this.executionStartedAt = executionStartedAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
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