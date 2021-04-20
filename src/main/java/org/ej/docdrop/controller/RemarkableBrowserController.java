package org.ej.docdrop.controller;

import org.ej.docdrop.domain.CachedDocumentInfo;
import org.ej.docdrop.service.RemarkableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/browse")
public class RemarkableBrowserController {

    private final RemarkableService service;

    public RemarkableBrowserController(RemarkableService service) {
        this.service = service;
    }

    @GetMapping(value = {"", "{folderId}"})
    String index(@PathVariable("folderId") Optional<UUID> folderId, Model model) {
        List<CachedDocumentInfo> folder = service.getFolder(folderId.orElse(null));
        model.addAttribute("documents", folder);

        return "remarkable/index";
    }

    @PostMapping("/refresh")
    String refresh() {
        service.refreshFileTree();

        return "redirect:/browse";
    }
}