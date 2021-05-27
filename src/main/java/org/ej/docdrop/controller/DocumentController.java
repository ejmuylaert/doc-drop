package org.ej.docdrop.controller;

import org.ej.docdrop.domain.Document;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping("/base")
public class DocumentController {

    private final static Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final String uploadDirectory;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    public DocumentController(
            @Value("${upload.path}") String uploadDirectory,
            DocumentService documentService,
            DocumentRepository documentRepository) {

        this.uploadDirectory = uploadDirectory;
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

    @GetMapping("/download/{id}")
    ResponseEntity<FileSystemResource> downloadFile(@PathVariable("id") Long id) {

        Document document = documentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        responseHeaders.setContentDisposition(ContentDisposition.attachment().filename(document.getDisplayName()).build());

        Path documentFilePath = Paths.get(uploadDirectory, document.getFilepath());

        return new ResponseEntity<>(new FileSystemResource(documentFilePath), responseHeaders,
                HttpStatus.OK);
    }
}