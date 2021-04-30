package org.ej.docdrop.controller;

import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService service;

    public FileController(FileService service) {
        this.service = service;
    }

    @GetMapping(value = {"", "{folderId}"})
    String index(@PathVariable Optional<UUID> folderId, Model model) {
        List<FileInfo> files = service.folder(folderId.orElse(null));
        List<FileInfo> path = service.folderPath(folderId.orElse(null));

        model.addAttribute("create_folder_url",
                "/files/create_folder" + folderId.map(id -> "/" + id).orElse(""));
        model.addAttribute("upload_url",
                "/files/upload" + folderId.map(id -> "/" + id).orElse(""));

        model.addAttribute("current_folder_id", folderId.orElse(null));
        model.addAttribute("files", files);
        model.addAttribute("path", path);

        return "file/index";
    }

    @PostMapping(value = {"/create_folder", "/create_folder/{parentId}"})
    String createFolder(@PathVariable("parentId") Optional<UUID> parentId,
                        @RequestParam String name,
                        RedirectAttributes attributes) {

        name = name.trim();
        if (name.isEmpty()) {
            attributes.addFlashAttribute("error_directory", "Name cannot be empty");
            return "redirect:/files";
        }

        attributes.addFlashAttribute("folder_message", "Directory created");
        service.createFolder(name, parentId.orElse(null));

        return "redirect:/files";
    }

    @PostMapping(value = {"/upload", "/upload/{parentId}"})
    String upload(@PathVariable("parentId") Optional<UUID> parentId,
                  @RequestParam("file") MultipartFile file,
                  RedirectAttributes attributes) throws IOException {

        if (file.isEmpty()) {
            attributes.addFlashAttribute("error", "Please select a file ...");
            return "redirect:/files";
        }

        Path tempFile = Files.createTempFile("docdrop_", null);
        file.transferTo(tempFile);
        service.addFile(file.getOriginalFilename(), tempFile, parentId.orElse(null));

        attributes.addFlashAttribute("message", "File uploaded ...");

        return "redirect:/files";
    }
}