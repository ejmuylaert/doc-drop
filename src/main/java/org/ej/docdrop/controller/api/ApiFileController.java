package org.ej.docdrop.controller.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
class ApiFileController {

    private final FileService fileService;

    public ApiFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(value = {"", "{folderId}"})
    FilesDTO index(@PathVariable Optional<UUID> folderId) {
        List<FileInfo> files = fileService.folder(folderId.orElse(null));
        List<FileInfo> path = fileService.folderPath(folderId.orElse(null));

        return new FilesDTO(files, path);
    }

    @PostMapping(value = {"", "{folderId}"})
    @ResponseStatus(HttpStatus.CREATED)
    FileInfo createFolder(@PathVariable Optional<UUID> folderId, @RequestBody CreateFolderDTO command) {
        return fileService.createFolder(command.getName(), folderId.orElse(null));
    }
}

class FilesDTO {
    private final List<FileInfo> files;
    private final List<FileInfo> path;

    public FilesDTO(List<FileInfo> files, List<FileInfo> path) {
        this.files = files;
        this.path = path;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public List<FileInfo> getPath() {
        return path;
    }
}

class CreateFolderDTO {
    private final String name;

    @JsonCreator
    public CreateFolderDTO(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}