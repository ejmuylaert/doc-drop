package org.ej.docdrop.controller;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {FileController.class})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FileService service;

    @Nested
    @DisplayName("Create directory")
    class CreateDirectory {

        @Test
        @DisplayName("Create folder")
        void createFolder() throws Exception {
            // Given
            FileInfo newFolder = new FileInfo(null, true, "my folder");
            when(service.createFolder(any(String.class), any())).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/files/create_folder")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "my folder"))
                    .andExpect(flash().attribute("folder_message", containsString("created")));

            verify(service).createFolder("my folder", null);
        }

        @Test
        @DisplayName("Create folder within folder")
        void createFolderWithinFolder() throws Exception {
            // Given
            UUID parentId = UUID.randomUUID();
            FileInfo newFolder = new FileInfo(null, true, "my folder");
            when(service.createFolder(any(String.class), any())).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/files/create_folder/{parentId}", parentId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "my folder"))
                    .andExpect(flash().attribute("folder_message", containsString("created")));

            verify(service).createFolder("my folder", parentId);
        }

        @Test
        @DisplayName("Redirect should go to created folder")
        void redirectToCurrent() throws Exception {
            // Given
            UUID parentId = UUID.randomUUID();
            FileInfo newFolder = new FileInfo(parentId, true, "my folder");
            when(service.createFolder(any(String.class), eq(parentId))).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/files/create_folder/{parentId}", parentId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "my folder"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlTemplate("/files/{parentId}", newFolder.getId()))
                    .andExpect(flash().attribute("folder_message", containsString("created")));
        }

        @Test
        @DisplayName("Display flash message when no name is given")
        void errorWhenNoName() throws Exception {
            // When, Then
            mockMvc.perform(post("/files/create_folder")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attribute("folder_error", containsString("empty")));
        }

        @Test
        @DisplayName("Strip spaces around name")
        void stripSpaces() throws Exception {
            // Given
            FileInfo newFolder = new FileInfo(null, true, "my folder");
            when(service.createFolder(any(String.class), any())).thenReturn(newFolder);

            // When, Then
            mockMvc.perform(post("/files/create_folder")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "    my folder   "))
                    .andExpect(flash().attribute("folder_message", containsString("created")));

            verify(service).createFolder("my folder", null);
        }
    }

    @Nested
    @DisplayName("Uploading file")
    class Upload {

        @Test
        @DisplayName("Display flash message when no file selected")
        void displayFlashWhenNoFile() throws Exception {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[]{});

            // When, Then
            mockMvc.perform(multipart("/files/upload").file(emptyFile).param("filename", ""))
                    .andExpect(status().is3xxRedirection()).andExpect(flash().attribute(
                    "upload_error",
                    containsString("file")));
        }

        @Test
        @DisplayName("Add file with on FileService")
        void addFile() throws Exception {
            // Given
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));

            // When, Then
            mockMvc.perform(multipart("/files/upload").file(dummyFile))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attribute("upload_message", containsString("uploaded")));

            ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
            verify(service).addFile(eq("original_filename"), pathArgumentCaptor.capture(),
                    eq(null));
            String contents = Files.readString(pathArgumentCaptor.getValue());
            assertThat(contents).isEqualTo("dummy document");
        }

        @Test
        @DisplayName("Add file in folder")
        void addFileInFolder() throws Exception {
            // Given
            UUID folderId = UUID.randomUUID();
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));

            // When, Then
            mockMvc.perform(multipart("/files/upload/{folderId}", folderId).file(dummyFile))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attribute("upload_message", containsString("uploaded")));

            ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
            verify(service).addFile(eq("original_filename"), pathArgumentCaptor.capture(),
                    eq(folderId));
            String contents = Files.readString(pathArgumentCaptor.getValue());
            assertThat(contents).isEqualTo("dummy document");
        }

        @Test
        @DisplayName("Stays in the current folder")
        void stayInCurrentFolder() throws Exception {
            // Given
            UUID folderId = UUID.randomUUID();
            MockMultipartFile dummyFile = new MockMultipartFile("file", "original_filename", null,
                    "dummy document".getBytes(StandardCharsets.UTF_8));

            // When, Then
            mockMvc.perform(multipart("/files/upload/{folderId}", folderId).file(dummyFile))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlTemplate("/files/{parentId}", folderId))
                    .andExpect(flash().attribute("upload_message", containsString("uploaded")));
        }
    }
}