package org.ej.docdrop.service;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.FileInfoRepository;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.springframework.stereotype.Service;

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

    FileInfo createFolder(String name, UUID parentFolder) {
        FileInfo info = new FileInfo(UUID.randomUUID(), parentFolder, true, name);
        fileInfoRepository.save(info);

        RemarkableCommand lastCommand = commandRepository.findFirstByOrderByCommandNumberDesc();
        long commandNumber = lastCommand == null ? 0 : lastCommand.getCommandNumber() + 1;
        CreateFolderCommand command = new CreateFolderCommand(info.getId(), commandNumber, name);
        commandRepository.save(command);

        return info;
    }

    void renameFile(UUID fileId, String newName) {
    }

    void removeFile(UUID fileId) {
    }
}