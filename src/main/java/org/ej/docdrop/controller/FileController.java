package org.ej.docdrop.controller;

import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
    RedirectView createFolder(@PathVariable("parentId") Optional<UUID> parentId,
                              @RequestParam String name,
                              RedirectAttributes attributes) {

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/files/{parentId}");

        name = name.trim();
        if (name.isEmpty()) {
            String id = parentId.map(UUID::toString).orElse("");
            attributes.addAttribute("parentId", id);
            attributes.addFlashAttribute("folder_error", "Name cannot be empty");

            return redirectView;
        }
        FileInfo folder = service.createFolder(name, parentId.orElse(null));

        attributes.addAttribute("parentId", folder.getId());
        attributes.addFlashAttribute("folder_message", "Directory created");

        return redirectView;
    }

    @PostMapping(value = {"/upload", "/upload/{parentId}"})
    RedirectView upload(@PathVariable("parentId") Optional<UUID> parentId,
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes attributes) throws IOException {

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/files/{parentId}");
        String id = parentId.map(UUID::toString).orElse("");
        attributes.addAttribute("parentId", id);

        if (file.isEmpty()) {
            attributes.addFlashAttribute("upload_error", "Please select a file ...");

            return redirectView;
        }

        Path tempFile = Files.createTempFile("docdrop_", null);
        file.transferTo(tempFile);
        service.addFile(file.getOriginalFilename(), tempFile, parentId.orElse(null));

        attributes.addFlashAttribute("upload_message", "File uploaded ...");

        return redirectView;
    }
}