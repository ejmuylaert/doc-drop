package org.ej.docdrop.domain;

import java.util.UUID;

public class SyncEvent {

    private final UUID fileId;
    private final long commandNumber;
    private SyncResult result;
    private String message;

    public static SyncEvent create(SyncCommand command, SyncResult result, String message) {
        return new SyncEvent(command.getFileId(), command.getCommandNumber(), result, message);
    }

    private SyncEvent(UUID fileId, long commandNumber, SyncResult result, String message) {
        this.fileId = fileId;
        this.commandNumber = commandNumber;
        this.result = result;
        this.message = message;
    }

    public void setResult(SyncResult result, String message) {
        this.result = result;
        this.message = message;
    }

    public UUID getFileId() {
        return fileId;
    }

    public long getCommandNumber() {
        return commandNumber;
    }

    public SyncResult getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
