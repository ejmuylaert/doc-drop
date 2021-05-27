package org.ej.docdrop.controller;

import org.ej.docdrop.domain.Document;
import org.ej.docdrop.repository.DocumentRepository;
import org.ej.docdrop.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {DocumentController.class})
@TestPropertySource(properties = {
		"upload.path=./testupload"
})
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
		mockMvc.perform(multipart("/base/upload").file(emptyFile).param("filename", ""))
				.andExpect(status().is3xxRedirection()).andExpect(flash().attribute("error",
				containsString("file")));
	}

	@Test
	void whenNoFilenameGiven_invokeDocumentServiceWithEmptyName() throws Exception {
		// Given
		MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
				"dummy document".getBytes(StandardCharsets.UTF_8));

		// When, Then
		mockMvc.perform(multipart("/base/upload").file(dummyFile).param("filename", ""))
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
		mockMvc.perform(multipart("/base/upload").file(dummyFile).param("filename", "given_filename"))
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
		mockMvc.perform(multipart("/base/upload").file(dummyFile).param("filename", "  "))
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
		mockMvc.perform(multipart("/base/upload").file(dummyFile).param("filename",
				"   given_filename" + "   "))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attribute("message", containsString("uploaded")));

		verify(documentService).store(any(Path.class), eq("original_filename"), eq(Optional.of(
				"given_filename")));
	}

	@Test
	void downloadFileSetNameOfFileInResponse() throws Exception {
		// Given
		Path path = Files.writeString(Paths.get("./testupload", "path"), "dummy");
		System.out.println(path.toAbsolutePath());

		when(repository.findById(10L)).thenReturn(Optional.of(new Document("path", "name.orig",
				null)));

		// When, Then
		mockMvc.perform(get("/base/download/10")).andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; " +
						"filename=\"name.orig\""))
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"));
	}

	@Test
	void returnFileNotFoundWhenIdNotInDb() throws Exception {
		// Given
		when(repository.findById(42L)).thenReturn(Optional.empty());

		// When, Then
		mockMvc.perform(get("/base/download/42")).andExpect(status().isNotFound());
	}
}