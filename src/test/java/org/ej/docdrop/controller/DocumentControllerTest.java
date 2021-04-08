package org.ej.docdrop.controller;

import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {DocumentController.class})
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentRepository repository;

    @Test
    void whenNoFileSelected_displayErrorFlashMessage() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[]{});

        // When, Then
        mockMvc.perform(multipart("/upload").file(emptyFile).param("filename", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("file")));
    }

    @Test
    void whenNoFilenameGiven_invokeDocumentServiceWithEmptyName() throws Exception {
        // Given
        MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                "dummy document".getBytes(StandardCharsets.UTF_8));

        // When, Then
        mockMvc.perform(multipart("/upload").file(dummyFile).param("filename", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message", containsString("uploaded")));

        verify(documentService).store(any(Path.class), eq("original_filename"),
                eq(Optional.empty()));
    }

    @Test
    void whenFilenameGiven_invokeDocumentServiceWithName() throws Exception {
        // Given
        MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                "dummy document".getBytes(StandardCharsets.UTF_8));

        // When, Then
        mockMvc.perform(multipart("/upload").file(dummyFile).param("filename", "given_filename"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message", containsString("uploaded")));

        verify(documentService).store(any(Path.class), eq("original_filename"), eq(Optional.of(
                "given_filename")));
    }

    @Test
    void whenFilenameOnlyContainsSpaces_invokeDocumentServiceWithoutName() throws Exception {
        // Given
        MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                "dummy document".getBytes(StandardCharsets.UTF_8));

        // When, Then
        mockMvc.perform(multipart("/upload").file(dummyFile).param("filename", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message", containsString("uploaded")));

        verify(documentService).store(any(Path.class), eq("original_filename"),
                eq(Optional.empty()));
    }

    @Test
    void whenFilenameGiven_invokeDocumentServiceWithTrimmedName() throws Exception {
        // Given
        MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                "dummy document".getBytes(StandardCharsets.UTF_8));

        // When, Then
        mockMvc.perform(multipart("/upload").file(dummyFile).param("filename", "   given_filename" +
                "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message", containsString("uploaded")));

        verify(documentService).store(any(Path.class), eq("original_filename"), eq(Optional.of(
                "given_filename")));
    }
}