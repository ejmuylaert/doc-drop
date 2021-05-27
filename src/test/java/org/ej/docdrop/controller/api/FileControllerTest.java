package org.ej.docdrop.controller.api;

import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ApiFileController.class})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService service;

    @Nested
    @DisplayName("Index")
    class Index {

        @Test
        @DisplayName("Get all files in root folder")
        void getAllFiles() throws Exception {
            // Given
            when(service.folder(null)).thenReturn(List.of(
                    new FileInfo(null, true, "Folder"),
                    new FileInfo(null, false, "File"),
                    new FileInfo(null, false, "Another file")));

            // When
            mockMvc.perform(get("/api/files").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.files[*]['name']", containsInAnyOrder("Folder", "File", "Another file")));
        }

        @Test
        @DisplayName("Get all files in folder with given id")
        void getAllFilesInFolder() throws Exception {
            // Given
            UUID parentId = UUID.randomUUID();
            when(service.folder(parentId)).thenReturn(List.of(
                    new FileInfo(parentId, true, "n - Folder"),
                    new FileInfo(parentId, false, "n - File"),
                    new FileInfo(parentId, false, "n - Another file")));

            // When
            mockMvc.perform(get("/api/files/{parentId}", parentId).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.files[*]['name']", containsInAnyOrder("n - Folder", "n - File", "n - " +
                            "Another file")));
        }


        @Test
        @DisplayName("File path is empty when fetching root folder")
        void pathIsEmptyForRootFolder() throws Exception {
            // Given
            when(service.folderPath(null)).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/files").accept(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.path", hasSize(0)));
        }

        @Test
        @DisplayName("Include parents in file path")
        void returnParentsInFilePath() throws Exception {
            // Given
            UUID firstParent = UUID.randomUUID();
            UUID secondParent = UUID.randomUUID();
            when(service.folderPath(null)).thenReturn(List.of(
                    new FileInfo(null, true, "Folder"),
                    new FileInfo(firstParent, false, "File"),
                    new FileInfo(secondParent, false, "Another file")));

            // When
            mockMvc.perform(get("/api/files").accept(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.path", hasSize(3)))
                    .andExpect(jsonPath("$.path[*]['parentId']", contains(null, firstParent.toString(),
                            secondParent.toString())));
        }
    }

    @Nested
    @DisplayName("Create folder")
    class CreateFolder {
        @Test
        @DisplayName("returns the newly created folder")
        void createFolder() throws Exception {
            // Given
            FileInfo newFolder = new FileInfo(null, true, "my folder");
            when(service.createFolder(any(String.class), any())).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/api/files").contentType(MediaType.APPLICATION_JSON).content("""
                            {"name": "my folder"}
                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("my folder")));

            verify(service).createFolder("my folder", null);
        }

        @Test
        @DisplayName("nest folder if requested")
        void createNestedFolder() throws Exception {
            // Given
            UUID parentId = UUID.randomUUID();
            FileInfo newFolder = new FileInfo(parentId, true, "my folder");
            when(service.createFolder(any(String.class), any())).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/api/files/{parentId}", parentId).contentType(MediaType.APPLICATION_JSON).content("""
                            {"name": "my folder"}
                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("my folder")))
                    .andExpect(jsonPath("$.parentId", is(parentId.toString())));

            verify(service).createFolder("my folder", parentId);
        }
    }

    @Nested
    @DisplayName("Upload file")
    class UploadFile {

        @Test
        @DisplayName("Add file with FileService")
        void addFileToRootFolder() throws Exception {
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));
            when(service.addFile(any(), any(), any())).thenReturn(new FileInfo(null, false, "original_filename"));

            // When, Then
            mockMvc.perform(multipart("/api/files/upload").file(dummyFile))
                    .andExpect(status().isCreated());

            ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
            verify(service).addFile(eq("original_filename"), pathArgumentCaptor.capture(), eq(null));
            String contents = Files.readString(pathArgumentCaptor.getValue());
            assertThat(contents).isEqualTo("dummy document");
        }

        @Test
        @DisplayName("Returns FileInfo of the just uploaded file")
        void returnFileInfo() throws Exception {
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));
            when(service.addFile(any(), any(), any())).thenReturn(new FileInfo(null, false, "original_filename"));

            // When, Then
            mockMvc.perform(multipart("/api/files/upload").file(dummyFile))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.folder", is(false)))
                    .andExpect(jsonPath("$.name", is("original_filename")));
        }

        @Test
        @DisplayName("can upload in specified folder")
        void uploadInFolder() throws Exception {
            // Given
            UUID parentId = UUID.randomUUID();
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));
            when(service.addFile(any(), any(), any())).thenReturn(new FileInfo(parentId, false, "original_filename"));

            // When, Then
            mockMvc.perform(multipart("/api/files/{folderId}/upload", parentId).file(dummyFile))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.parentId", is(parentId.toString())))
                    .andExpect(jsonPath("$.folder", is(false)))
                    .andExpect(jsonPath("$.name", is("original_filename")));

            ArgumentCaptor<UUID> pathArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(service).addFile(eq("original_filename"), any(Path.class), pathArgumentCaptor.capture());
            assertThat(pathArgumentCaptor.getValue()).isEqualTo(parentId);
        }
    }
}
