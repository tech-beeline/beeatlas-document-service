package ru.beeline.documentservice.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.beeline.documentservice.domain.DocumentationType;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.exception.ValidationException;
import ru.beeline.documentservice.repository.DocumentRepository;
import ru.beeline.documentservice.repository.DocumentationTypeRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceBinaryUploadTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentationTypeRepository documentationTypeRepository;

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "bucketName", "test-bucket");
    }

    @Test
    void uploadBinaryFile_requiresFileName() {
        assertThrows(ValidationException.class,
                () -> documentService.uploadBinaryFile("abc".getBytes(),
                        false,
                        "patterns",
                        "md",
                        1,
                        null,
                        null,
                        "application/octet-stream"));
    }

    @Test
    void uploadBinaryFile_requiresExtensionInFileName() {
        assertThrows(ValidationException.class,
                () -> documentService.uploadBinaryFile("abc".getBytes(),
                        false,
                        "patterns",
                        "md",
                        1,
                        "file",
                        null,
                        "application/octet-stream"));
    }

    @Test
    void uploadBinaryFile_withoutTargetId_rejectsRegisteredFolder() {
        when(documentationTypeRepository.findByFolder("patterns")).thenReturn(Optional.of(new DocumentationType()));

        assertThrows(ValidationException.class,
                () -> documentService.uploadBinaryFile("abc".getBytes(),
                        false,
                        "patterns",
                        "md",
                        1,
                        "a.md",
                        null,
                        "application/octet-stream"));
    }

    @Test
    void uploadBinaryFile_withTargetId_requiresKnownFolderAndMatchingExtension() {
        when(documentationTypeRepository.findByFolder("patterns")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> documentService.uploadBinaryFile("abc".getBytes(),
                        false,
                        "patterns",
                        "md",
                        1,
                        "a.md",
                        10,
                        "application/octet-stream"));

        DocumentationType type = DocumentationType.builder().folder("patterns").docType("md").build();
        when(documentationTypeRepository.findByFolder("patterns")).thenReturn(Optional.of(type));

        assertThrows(ValidationException.class,
                () -> documentService.uploadBinaryFile("abc".getBytes(),
                        false,
                        "patterns",
                        "md",
                        1,
                        "a.pdf",
                        10,
                        "application/octet-stream"));
    }

    @Test
    void uploadBinaryFile_success_savesToS3AndDb() throws Exception {
        ReflectionTestUtils.setField(documentService, "bucketName", "bucket");

        DocumentationType type = DocumentationType.builder().folder("patterns").docType("md").build();
        when(documentationTypeRepository.findByFolder("patterns")).thenReturn(Optional.of(type));
        when(documentRepository.existsByKey(any())).thenReturn(false);
        when(documentRepository.save(any())).thenAnswer(invocation -> {
            S3Document doc = invocation.getArgument(0);
            doc.setId(777);
            return doc;
        });

        var dto = documentService.uploadBinaryFile("abc".getBytes(),
                false,
                "patterns",
                "md",
                1,
                "a.md",
                10,
                "application/octet-stream");

        assertEquals(777, dto.getDocId());

        ArgumentCaptor<S3Document> captor = ArgumentCaptor.forClass(S3Document.class);
        verify(documentRepository).save(captor.capture());
        assertEquals("patterns/a.md", captor.getValue().getKey());
        assertEquals("md", captor.getValue().getDocType());
        assertEquals("USER", captor.getValue().getSourceType());
        assertEquals(10, captor.getValue().getTargetEntityId());
    }
}