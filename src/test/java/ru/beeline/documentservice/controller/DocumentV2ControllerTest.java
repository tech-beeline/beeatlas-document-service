package ru.beeline.documentservice.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.TestPropertySource;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.service.DocumentService;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentV2Controller.class)
@Import(CustomExceptionHandler.class)
@TestPropertySource(properties = {"management.endpoints.web.discovery.enabled=false", "management.endpoints.web.base-path="})
class DocumentV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private WebEndpointsSupplier webEndpointsSupplier;

    @MockBean
    private ServletEndpointsSupplier servletEndpointsSupplier;

    @MockBean
    private ControllerEndpointsSupplier controllerEndpointsSupplier;

    @MockBean
    private EndpointMediaTypes endpointMediaTypes;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @MockBean
    private CorsEndpointProperties corsEndpointProperties;

    @TestConfiguration
    static class ActuatorPropsConfig {
        @Bean
        WebEndpointProperties webEndpointProperties() {
            WebEndpointProperties props = new WebEndpointProperties();
            props.setBasePath("");
            return props;
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void setupActuatorSupplierMocks() {
        Mockito.when(webEndpointsSupplier.getEndpoints()).thenReturn(Collections.emptyList());
        Mockito.when(servletEndpointsSupplier.getEndpoints()).thenReturn(Collections.emptyList());
        Mockito.when(controllerEndpointsSupplier.getEndpoints()).thenReturn(Collections.emptyList());
        Mockito.when(corsEndpointProperties.toCorsConfiguration()).thenReturn(null);
    }

    @Test
    void uploadBinaryFile_rejectsInvalidContentLength() throws Exception {
        mockMvc.perform(post("/api/v2/documents/patterns/md").queryParam("fileName", "a.md")
                                .header("Content-Length", "0")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .content("abc".getBytes())).andExpect(status().isBadRequest());
    }

    @Test
    void uploadBinaryFile_rejectsContentLengthMismatch() throws Exception {
        mockMvc.perform(post("/api/v2/documents/patterns/md").queryParam("fileName", "a.md")
                                .header("Content-Length", "10")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .content("abc".getBytes())).andExpect(status().isBadRequest());
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

        mockMvc.perform(post("/api/v2/documents/patterns/md").queryParam("fileName", "a.md")
                                .header("Content-Length", "3")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .content("abc".getBytes())).andExpect(status().isOk());
    }
}

