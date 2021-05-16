package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.UploadFileCommand;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SyncService {

    private final RemarkableCommandRepository repository;
    private final RemarkableClient client;
    private final Path storageDirectory;

    public SyncService(RemarkableCommandRepository repository, RemarkableClient client, @Value("${storage.path}") String storageDirectory) {
        this.repository = repository;
        this.client = client;
        this.storageDirectory = Paths.get(storageDirectory);
    }

    public void sync() {
        Iterable<RemarkableCommand> pendingCommands = repository.pendingCommands();

        pendingCommands.forEach(command -> {

        });
    }

    SyncEvent execute(CreateFolderCommand command) {
        if (!client.folderExists(command.getParentId())) {
            return SyncEvent.create(command, SyncEvent.Result.PRE_CONDITION_FAILED, "Parent folder does not exist");
        }
        if (client.folderExists(command.getFileId())) {
            return SyncEvent.create(command, SyncEvent.Result.PRE_CONDITION_FAILED, "Folder already exists");
        }

        client.createFolder(command.getFileId(), command.getName());

        return SyncEvent.create(command, SyncEvent.Result.SUCCESS, null);
    }

    SyncEvent execute(UploadFileCommand command) {

        Path path = storageDirectory.resolve(command.getFileId().toString());
        Path thumbnailPath = storageDirectory.resolve(command.getFileId() + ".thumbnail");

        client.uploadFile(command.getFileId(), command.getParentId(), command.getName(), path, thumbnailPath);

        return SyncEvent.create(command, SyncEvent.Result.SUCCESS, "");
    }
}