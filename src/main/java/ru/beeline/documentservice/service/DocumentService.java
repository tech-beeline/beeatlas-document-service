package ru.beeline.documentservice.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.beeline.documentservice.client.CamundaClient;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.dto.CamundaProcessRequestDTO;
import ru.beeline.documentservice.dto.CamundaVariableDTO;
import ru.beeline.documentservice.exception.ForbiddenException;
import ru.beeline.documentservice.exception.NotFoundException;
import ru.beeline.documentservice.exception.S3Exception;
import ru.beeline.documentservice.exception.ValidationException;
import ru.beeline.documentservice.repository.DocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public ResponseEntity<byte[]> getDocument(Integer id, String userRoles, Integer userId) {
        Optional<S3Document> optionalS3Document = documentRepository.findById(id);
        if (optionalS3Document.isEmpty()) {
            throw new NotFoundException("404: Запись с данным id не найдена");
        }
        S3Document document = optionalS3Document.get();
        String key = document.getKey();
        byte[] result;
        if (document.getIsPublic() ||
                (userRoles != null && userRoles.contains("ADMINISTRATOR")) ||
                (document.getSourceType().equals("user") && document.getSourceId().equals(userId))) {
            result = downloadDocumentFromS3(key);
        } else {
            throw new ForbiddenException("Доступ запрещен");
        }
        String documentKey = document.getKey();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(result.length);
        String fileName = extractFileNameFromKey(documentKey);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    private byte[] downloadDocumentFromS3(String key) {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build());
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new S3Exception("Ошибка при загрузке документа: " + e.getMessage());
        }
    }

    private String extractFileNameFromKey(String key) {
        String[] parts = key.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : key;
    }

    private void validateRequest(HttpServletRequest request, String userRoles) {
        if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
            throw new ValidationException("Не верный заголовок: Content-Type");
        }
        String contentDisposition = request.getHeader("Content-Disposition");
        if (contentDisposition == null || !contentDisposition.contains("filename=")) {
            throw new ValidationException("Не верный заголовок: Content-Disposition");
        }
        List<String> roles = Arrays.asList(userRoles.split(","));
        if (!roles.contains("ADMINISTRATOR")) {
            throw new ForbiddenException("Доступ запрещен");
        }
    }

    public ResponseEntity<String> uploadFileToS3(MultipartFile file, Boolean sync, String userRoles,
                                                 Integer userId, String entityType, HttpServletRequest request) {
        validateRequest(request, userRoles);
        String fileName = removePrefix(request.getHeader("Content-Disposition"), "filename=");
        if (fileName.isEmpty()) {
            throw new ValidationException("Отсутствует имя файла в заголовке Content-Disposition, filename");
        }
        uploadFile(fileName, file);
        Integer docId = saveDocumentInfo(fileName, userId);
        CamundaProcessRequestDTO requestBody = new CamundaProcessRequestDTO();
        Map<String, CamundaVariableDTO> variables = new HashMap<>();
        variables.put("entityType", new CamundaVariableDTO(entityType, "String"));
        variables.put("sync", new CamundaVariableDTO(sync != null ? sync : false, "Boolean"));
        variables.put("docId", new CamundaVariableDTO(docId, "Integer"));
        requestBody.setVariables(variables);
        requestBody.setBusinessKey(docId);
        String response = camundaClient.postCamunda(requestBody);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"docId\": " + docId + "}");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Failed to start Camunda process");
        }

    }

    private static String removePrefix(String original, String prefix) {
        if (original.startsWith(prefix)) {
            return original.substring(prefix.length());
        } else {
            return original;
        }
    }

    private Integer saveDocumentInfo(String fileName, Integer userId) {
        S3Document document = new S3Document();
        document.setDocType("excel");
        document.setKey("/import/" + fileName);
        document.setSourceType("USER");
        document.setSourceId(userId);
        document.setIsPublic(true);
        document.setCreatedDate(LocalDateTime.now());
        return documentRepository.save(document).getId();
    }

    public void uploadFile(String fileName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Файл отсутствует или пуст");
        }
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object("/import/" + fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("Файл успешно загружен: " + fileName);
        } catch (Exception e) {
            log.error("Не удалось загрузить файл: " + e.getMessage());
        }
    }
}

