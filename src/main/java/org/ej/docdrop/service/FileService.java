package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final RemarkableCommandRepository commandRepository;

    public FileService(FileInfoRepository fileInfoRepository,
                       RemarkableCommandRepository commandRepository) {

        this.fileInfoRepository = fileInfoRepository;
        this.commandRepository = commandRepository;
    }

    List<FileInfo> folder(UUID parentId) {
        return fileInfoRepository.getFileInfoByParentId(parentId);
    }

    Iterable<RemarkableCommand> pendingCommands() {
        return commandRepository.findAllByOrderByCommandNumberAsc();
    }

    void addFile(String name, Path filePath, UUID parentFolder) {
    }

    @Transactional
    public FileInfo createFolder(String name, UUID parentFolderId) {
        if (parentFolderId != null) {
            // When not in the root folder, check if parent exists and is a folder
            fileInfoRepository.findById(parentFolderId)
                    .filter(FileInfo::isFolder)
                    .orElseThrow(() -> new RuntimeException("Parent folder doesn't exist, id: " + parentFolderId));
        }

        FileInfo info = new FileInfo(UUID.randomUUID(), parentFolderId, true, name);
        fileInfoRepository.save(info);

        RemarkableCommand lastCommand = commandRepository.findFirstByOrderByCommandNumberDesc();
        long commandNumber = lastCommand == null ? 0 : lastCommand.getCommandNumber() + 1;
        CreateFolderCommand command = new CreateFolderCommand(info.getId(), commandNumber, name,
                parentFolderId);
        commandRepository.save(command);

        return info;
    }

    void renameFile(UUID fileId, String newName) {
    }

    void removeFile(UUID fileId) {
    }
}