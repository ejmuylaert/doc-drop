package org.ej.docdrop.controller.api;

import org.ej.docdrop.domain.FileInfo;
import org.ej.docdrop.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }
}
