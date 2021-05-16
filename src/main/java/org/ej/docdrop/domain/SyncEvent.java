package org.ej.docdrop.domain;

import java.time.Instant;
import java.util.UUID;

public class SyncEvent {

    private final UUID id;
    private final Instant createdAt;
    private final UUID fileId;
    private final long commandNumber;
    private Result result;
    private String message;

    public static SyncEvent create(RemarkableCommand command, Result result, String message) {
        return new SyncEvent(UUID.randomUUID(), Instant.now(), command.getFileId(), command.getCommandNumber(), result, message);
    }

    private SyncEvent(UUID id, Instant createdAt, UUID fileId, long commandNumber, Result result, String message) {
        this.id = id;
        this.createdAt = createdAt;
        this.fileId = fileId;
        this.commandNumber = commandNumber;
        this.result = result;
        this.message = message;
    }

    public void setResult(Result result, String message) {
        this.result = result;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getFileId() {
        return fileId;
    }

    public long getCommandNumber() {
        return commandNumber;
    }

    public Result getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public enum Result {
        SUCCESS,
        CLIENT_NOT_AVAILABLE,
        PRE_CONDITION_FAILED,
        EXECUTION_FAILED
    }
}
