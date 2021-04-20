package org.ej.docdrop.controller;

import org.ej.docdrop.domain.CachedDocumentInfo;
import org.ej.docdrop.service.RemarkableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {RemarkableBrowserController.class})
@TestPropertySource(properties = {
        "upload.path=./testupload"
})
class RemarkableBrowserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RemarkableService mockService;

    @Test
    void whenNoFilesInCacheShowMessage() {

    }

    @Test
    void whenRefreshShowMessage() {

    }

    @Test
    void indexReturnsRootFiles() throws Exception {
        // Given
        when(mockService.getFolder(null)).thenReturn(List.of(
                new CachedDocumentInfo(UUID.randomUUID(), null, false, "file - 1"),
                new CachedDocumentInfo(UUID.randomUUID(), null, false, "file - 2")
        ));

        // When
        mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file - 1")))
                .andExpect(content().string(containsString("file - 2")));
    }

    @Test
    void acceptFolderIdInPath() throws Exception {
        // Given
        UUID folderId = UUID.randomUUID();
        when(mockService.getFolder(folderId)).thenReturn(List.of(
                new CachedDocumentInfo(UUID.randomUUID(), null, false, "file - 3"),
                new CachedDocumentInfo(UUID.randomUUID(), null, false, "file - 4")
        ));

        // When
        mockMvc.perform(get("/browse/{uuid}", folderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file - 3")))
                .andExpect(content().string(containsString("file - 4")));
    }
}