package org.ej.docdrop.controller;

import org.ej.docdrop.domain.Document;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Controller
public class DocumentController {

    private final static Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    public DocumentController(DocumentService documentService,
                              DocumentRepository documentRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
    }

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("documents", documentRepository.findAll());
        Iterable<Document> all = documentRepository.findAll();
        return "document/index";
    }

    @PostMapping("/upload")
    String uploadFile(@RequestParam("file") MultipartFile file,
                      @RequestParam("filename") String filename,
                      RedirectAttributes attributes) throws IOException {

        if (file.isEmpty()) {
            attributes.addFlashAttribute("error", "Please select a file ...");

            return "redirect:/";
        }

        Path tempFile = Files.createTempFile("docdrop_", null);
        file.transferTo(tempFile);

        filename = filename.trim();
        Optional<String> givenName = filename.length() > 0 ? Optional.of(filename) :
                Optional.empty();

        documentService.store(tempFile, file.getOriginalFilename(), givenName);

        attributes.addFlashAttribute("message", "File uploaded");

        return "redirect:/";
    }
}