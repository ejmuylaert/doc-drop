package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.UploadFileCommand;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static org.ej.docdrop.domain.SyncResult.*;

@Component
public class SyncCommandHandler {

    private final RemarkableClient client;
    private final FileStorage storage;

    public SyncCommandHandler(RemarkableClient client, FileStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    public SyncEvent apply(CreateFolderCommand command) throws ConnectionException {
        try {
            if (command.getParentId() != null && !client.folderExists(command.getParentId())) {
                return SyncEvent.create(command, PRE_CONDITION_FAILED, "Parent folder does not exist");
            }
            if (client.folderExists(command.getFileId())) {
                return SyncEvent.create(command, PRE_CONDITION_FAILED, "Folder already exists");

            }
            client.createFolder(command.getFileId(), command.getName());
            return SyncEvent.create(command, SUCCESS, "");

        } catch (RemarkableClientException e) {
            return SyncEvent.create(command, EXECUTION_FAILED, e.getMessage());
        }
    }

    public SyncEvent apply(UploadFileCommand command) throws ConnectionException {
        try {
            if (!client.folderExists(command.getParentId())) {
                return SyncEvent.create(command, PRE_CONDITION_FAILED, "Parent folder does not exists");
            }
            if (client.fileExists(command.getFileId())) {
                return SyncEvent.create(command, PRE_CONDITION_FAILED, "File already exists");
            }

            Path filePath = storage.getFilePath(command.getFileId());
            Path thumbnailPath = storage.getThumbnailPath(command.getFileId());
            client.uploadFile(command.getFileId(), command.getParentId(), command.getName(), filePath, thumbnailPath);

            return SyncEvent.create(command, SUCCESS, "");

        } catch (RemarkableClientException e) {
            return SyncEvent.create(command, EXECUTION_FAILED, e.getMessage());
        }
    }
}
