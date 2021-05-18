package org.ej.docdrop.service;

import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SyncService {

    private final SyncCommandRepository repository;
    private final RemarkableClient client;
    private final Path storageDirectory;

    public SyncService(SyncCommandRepository repository, RemarkableClient client, @Value("${storage.path}") String storageDirectory) {
        this.repository = repository;
        this.client = client;
        this.storageDirectory = Paths.get(storageDirectory);
    }

    public void sync() {
        Iterable<SyncCommand> pendingCommands = repository.pendingCommands();

        pendingCommands.forEach(command -> {

        });
    }
}