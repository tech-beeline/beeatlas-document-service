package ru.beeline.documentservice.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.service.DocumentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentV2Controller.class)
@Import(CustomExceptionHandler.class)
@TestPropertySource(
        properties = {
                "management.endpoints.web.discovery.enabled=false",
                "management.endpoints.web.base-path=",
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        })
class DocumentV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void uploadBinaryFile_rejectsInvalidContentLength() throws Exception {
        mockMvc.perform(post("/api/v2/documents/patterns/md")
                        .queryParam("fileName", "a.md")
                        .header("Content-Length", "0")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("abc".getBytes()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadBinaryFile_rejectsContentLengthMismatch() throws Exception {
        mockMvc.perform(post("/api/v2/documents/patterns/md")
                        .queryParam("fileName", "a.md")
                        .header("Content-Length", "10")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("abc".getBytes()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadBinaryFile_success() throws Exception {
        Mockito.when(documentService.uploadBinaryFile(Mockito.any(),
                        Mockito.anyBoolean(),
                        Mockito.eq("patterns"),
                        Mockito.eq("md"),
                        Mockito.isNull(),
                        Mockito.eq("a.md"),
                        Mockito.isNull(),
                        Mockito.eq(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                .thenReturn(DocIdDTO.builder().docId(12345).build());

        mockMvc.perform(post("/api/v2/documents/patterns/md")
                        .queryParam("fileName", "a.md")
                        .header("Content-Length", "3")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("abc".getBytes()))
                .andExpect(status().isOk());
    }
}