package org.ej.docdrop.domain;

import org.ej.docdrop.serializers.SyncResultType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@IdClass(SyncCommand.CommandId.class)
@TypeDef(
        name = "pgsql_enum",
        typeClass = SyncResultType.class
)
public abstract class SyncCommand {

    @Id
    private final UUID fileId;
    @Id
    private final long commandNumber;
    private final Instant createdAt;
    private Instant syncedAt;
    @Type( type = "pgsql_enum" )
    @Column(
            columnDefinition = "sync_result"
    )
    private SyncResult syncResult;
    private String syncMessage;

    protected SyncCommand() {
        this.fileId = null;
        this.commandNumber = 1;
        this.createdAt = null;
    }

    public SyncCommand(UUID fileId, long commandNumber, Instant createdAt) {
        this.fileId = fileId;
        this.commandNumber = commandNumber;
        this.createdAt = createdAt;
    }

    public UUID getFileId() {
        return fileId;
    }

    public long getCommandNumber() {
        return commandNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
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