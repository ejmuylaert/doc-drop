package org.ej.docdrop.service;

import org.ej.docdrop.domain.*;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@Service
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final RemarkableCommandRepository commandRepository;
    private final Path storageDirectory;

    public FileService(FileInfoRepository fileInfoRepository,
                       RemarkableCommandRepository commandRepository,
                       @Value("${storage.path}") String storageDirectory) {

        this.fileInfoRepository = fileInfoRepository;
        this.commandRepository = commandRepository;
        this.storageDirectory = Paths.get(storageDirectory);
    }

    public List<FileInfo> folder(UUID parentId) {
        return fileInfoRepository.getFileInfoByParentId(parentId);
    }

    Iterable<RemarkableCommand> pendingCommands() {
        return commandRepository.findAllByOrderByCommandNumberAsc();
    }

    public Path filePathForId(UUID fileId) {
        return storageDirectory.resolve(fileId.toString());
    }

    public Path thumbnailFor(UUID fileId) {
        return storageDirectory.resolve(fileId + ".thumbnail");
    }

    public FileInfo getFile(UUID fileId) {
        return fileInfoRepository.findById(fileId).get();
    }

    @Transactional
    public void addFile(String name, Path filePath, Path thumbnailPath, UUID parentFolderId) {
        assertFolderExist(parentFolderId);

        FileInfo info = new FileInfo(parentFolderId, false, name);
        Path targetFilePath = storageDirectory.resolve(info.getId().toString());
        Path targetThumbnailPath = storageDirectory.resolve(info.getId() + ".thumbnail");

        try {
            Files.move(filePath, targetFilePath);
            Files.move(thumbnailPath, targetThumbnailPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move uploaded file and/or thumbnail file", e);
        }

        fileInfoRepository.save(info);
        saveCommand(number -> new UploadFileCommand(info.getId(), number, name, parentFolderId));
    }

    @Transactional
    public FileInfo createFolder(String name, UUID parentFolderId) {
        assertFolderExist(parentFolderId);

        FileInfo info = new FileInfo(parentFolderId, true, name);
        fileInfoRepository.save(info);
        saveCommand(number -> new CreateFolderCommand(info.getId(), number, name, parentFolderId));

        return info;
    }

    @Transactional
    public void renameFile(UUID fileId, String newName) {
        FileInfo fileInfo = fileInfoRepository
                .findById(fileId)
                .orElseThrow(() -> new RuntimeException("Original file not found"));

        fileInfo.setName(newName);
        fileInfoRepository.save(fileInfo);
        saveCommand(number -> new RenameCommand(fileId, number, newName));
    }

    @Transactional
    public FileInfo removeFile(UUID fileId) {
        List<FileInfo> files = fileInfoRepository.getFileInfoByParentId(fileId);
        if (files.size() > 0) {
            throw new RuntimeException("Folder not empty");
        }

        FileInfo file = fileInfoRepository
                .findById(fileId)
                .orElseThrow(() -> new RuntimeException("Could not find file with id: " + fileId));

        fileInfoRepository.deleteById(fileId);
        saveCommand(number -> new DeleteCommand(fileId, number));

        return file;
    }

    private void assertFolderExist(UUID parentFolderId) {
        if (parentFolderId != null) {
            // When not in the root folder, check if parent exists and is a folder
            fileInfoRepository.findById(parentFolderId)
                    .filter(FileInfo::isFolder)
                    .orElseThrow(() -> new RuntimeException("Parent folder doesn't exist, id: " + parentFolderId));
        }
    }

    private void saveCommand(Function<Long, RemarkableCommand> commandCreator) {
        RemarkableCommand lastCommand = commandRepository.findFirstByOrderByCommandNumberDesc();
        long number = lastCommand == null ? 0 : lastCommand.getCommandNumber() + 1;

        RemarkableCommand command = commandCreator.apply(number);
        commandRepository.save(command);
    }

    public List<FileInfo> folderPath(UUID folderId) {
        List<FileInfo> path = new ArrayList<>();

        Optional<FileInfo> folder;
        while (folderId != null) {
            folder = fileInfoRepository.findById(folderId);

            folder.ifPresent(path::add);
            folderId = folder.map(FileInfo::getParentId).orElse(null);
        }

        Collections.reverse(path);
        return path;
    }
}