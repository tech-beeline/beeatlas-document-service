package ru.beeline.documentservice.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.beeline.documentservice.client.CamundaClient;
import ru.beeline.documentservice.client.PackageClient;
import ru.beeline.documentservice.controller.RequestContext;
import ru.beeline.documentservice.domain.DocumentationType;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.dto.*;
import ru.beeline.documentservice.exception.ForbiddenException;
import ru.beeline.documentservice.exception.NotFoundException;
import ru.beeline.documentservice.exception.S3Exception;
import ru.beeline.documentservice.exception.ValidationException;
import ru.beeline.documentservice.mapper.DocumentExportMapper;
import ru.beeline.documentservice.mapper.DocumentImportMapper;
import ru.beeline.documentservice.repository.DocumentRepository;
import ru.beeline.documentservice.repository.DocumentationTypeRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private PackageClient packageClient;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentationTypeRepository documentationTypeRepository;

    @Autowired
    private DocumentImportMapper documentImportMapper;

    @Autowired
    private DocumentExportMapper documentExportMapper;

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
        if (document.getIsPublic() || (userRoles != null && userRoles.contains("ADMINISTRATOR")) || (document.getSourceType()
                .equals("user") && document.getSourceId().equals(userId))) {
            result = downloadDocumentFromS3(key);
        } else {
            throw new ForbiddenException("Доступ запрещен");
        }
        String fileName = extractFileNameFromKey(key);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(result.length);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        return ResponseEntity.ok().headers(headers).body(result);
    }

    private byte[] downloadDocumentFromS3(String key) {
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
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

    private void validateRequest(HttpServletRequest request) {
        String contentDisposition = request.getHeader("Content-Disposition");
        if (contentDisposition == null) {
            throw new ValidationException("Отсутствует заголовок Content-Disposition");
        }
        if (!RequestContext.getRoles().contains("ADMINISTRATOR")) {
            throw new ForbiddenException("403 Forbidden.");
        }
    }

    public DocIdDTO uploadFileAndStartProcess(MultipartFile file,
                                              Boolean sync,
                                              Integer userId,
                                              String entityType,
                                              HttpServletRequest request) {
        validateRequest(request);
        String fileName = request.getHeader("Content-Disposition");
        if (fileName.isEmpty()) {
            throw new ValidationException("Отсутствует имя файла в заголовке Content-Disposition");
        }
        fileName = "import/" + fileName;
        uploadFile(fileName, file);
        Integer docId = saveDocumentInfo(fileName, userId, "excel", "USER", true, entityType, "import");
        CamundaProcessRequestDTO requestBody = new CamundaProcessRequestDTO();
        Map<String, CamundaVariableDTO> variables = new HashMap<>();
        variables.put("entityType", new CamundaVariableDTO(entityType, "String"));
        variables.put("sync", new CamundaVariableDTO(sync != null ? sync : false, "Boolean"));
        variables.put("docId", new CamundaVariableDTO(docId, "Integer"));
        requestBody.setVariables(variables);
        requestBody.setBusinessKey(docId);
        String response = camundaClient.postCamunda(requestBody);
        if (response != null) {
            return DocIdDTO.builder().docId(docId).build();
        } else {
            throw new S3Exception("Failed to start Camunda process");
        }
    }

    private Integer saveDocumentInfo(String fileName,
                                     Integer sourceId,
                                     String docType,
                                     String sourceType,
                                     Boolean isPublic,
                                     DocumentationType documentationType,
                                     Integer targetId) {
        S3Document document = new S3Document();
        document.setDocType(docType);
        document.setKey(fileName);
        document.setSourceType(sourceType);
        document.setSourceId(sourceId);
        document.setIsPublic(isPublic);
        document.setCreatedDate(LocalDateTime.now());
        document.setDocumentationType(documentationType);
        document.setTargetEntityId(targetId);
        return documentRepository.save(document).getId();
    }

    private Integer saveDocumentInfo(String fileName,
                                     Integer sourceId,
                                     String docType,
                                     String sourceType,
                                     Boolean isPublic,
                                     String entityType,
                                     String operationType) {
        S3Document document = new S3Document();
        document.setDocType(docType);
        document.setKey(fileName);
        document.setSourceType(sourceType);
        document.setSourceId(sourceId);
        document.setIsPublic(isPublic);
        document.setEntityType(entityType);
        document.setOperationType(operationType);
        document.setCreatedDate(LocalDateTime.now());
        return documentRepository.save(document).getId();
    }

    public void uploadFile(String fileName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Файл отсутствует или пуст");
        }
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                                          .bucket(bucketName)
                                          .object(fileName)
                                          .stream(inputStream, file.getSize(), -1)
                                          .contentType(file.getContentType())
                                          .build());
            log.info("Файл успешно загружен: " + fileName);
        } catch (Exception e) {
            log.error("Не удалось загрузить файл", e);
            throw new S3Exception("Не удалось загрузить файл в S3");
        }
    }

    public DocIdDTO uploadExcelFile(MultipartFile file,
                                    Boolean isPublic,
                                    String pathName,
                                    String docType,
                                    Integer userId,
                                    String contentDisposition,
                                    Integer targetId) {
        DocumentationType documentationType = null;
        if (Objects.nonNull(targetId)) {
            documentationType = documentationTypeRepository.findByFolder(pathName)
                    .orElseThrow(() -> new ValidationException("Неизвестный тип документации"));

            Pattern pattern = Pattern.compile("filename=\"[^\"]*\\.([^\"]+)\"");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (!matcher.group(1).equals(documentationType.getDocType())) {
                throw new ValidationException("Расширение не соответсвует типу документации");
            }

        } else {
            if (documentationTypeRepository.findByFolder(pathName).isPresent()) {
                throw new ValidationException("Не передан id документируемой сущности");
            }

        }
        String fileName = contentDisposition;
        validationUploadExcelFile(contentDisposition, fileName);
        fileName = pathName + "/" + fileName;
        uploadFile(fileName, file);
        String sourceType = userId != null ? "USER" : "SYSTEM";
        return DocIdDTO.builder()
                .docId(saveDocumentInfo(fileName, userId, docType, sourceType, isPublic, documentationType, targetId))
                .build();
    }

    private void validationUploadExcelFile(String contentDisposition, String fileName) {
        if (contentDisposition == null) {
            throw new ValidationException("Отсутствует заголовок Content-Disposition");
        }
        if (fileName.isEmpty()) {
            throw new ValidationException("Отсутствует имя файла в заголовке Content-Disposition");
        }
    }

    public void documentReloading(Integer docId, MultipartFile file, String contentDisposition) {
        S3Document s3Document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("Запись с данным id не найдена"));
        if (!(s3Document.getKey() == null || s3Document.getKey().isEmpty())) {
            throw new ValidationException("Документ уже загружен.");
        }
        String fileName = contentDisposition;
        validationUploadExcelFile(contentDisposition, fileName);
        fileName = "export/" + fileName;
        uploadFile(fileName, file);
        s3Document.setKey(fileName);
        s3Document.setLastModifiedDate(LocalDateTime.now());
        documentRepository.save(s3Document);
    }

    public DocIdDTO asynchronousDocumentLoading(String entityType, Integer userId) {

        Integer docId = saveDocument("excel", userId, "USER", true, entityType, "export");
        CamundaProcessRequestExportDTO requestBody = new CamundaProcessRequestExportDTO();
        Map<String, CamundaVariableDTO> variables = new HashMap<>();
        variables.put("entityType", new CamundaVariableDTO(entityType, "String"));
        variables.put("docId", new CamundaVariableDTO(docId, "Integer"));
        variables.put("userId", new CamundaVariableDTO(userId, "Integer"));
        requestBody.setVariables(variables);
        requestBody.setBusinessKey(docId + "export");
        String response = camundaClient.postCamunda(requestBody);
        if (response != null) {
            return DocIdDTO.builder().docId(docId).build();
        } else {
            throw new S3Exception("Failed to start Camunda process");
        }
    }

    private Integer saveDocument(String docType,
                                 Integer sourceId,
                                 String sourceType,
                                 Boolean isPublic,
                                 String entityType,
                                 String operationType) {
        S3Document document = new S3Document();
        document.setDocType(docType);
        document.setSourceType(sourceType);
        document.setSourceId(sourceId);
        document.setIsPublic(isPublic);
        document.setEntityType(entityType);
        document.setOperationType(operationType);
        document.setCreatedDate(LocalDateTime.now());
        return documentRepository.save(document).getId();
    }

    public List<DocumentImportDTO> getDocumentsImport(Integer userId) {
        if (!RequestContext.getRoles().contains("ADMINISTRATOR")) {
            throw new ForbiddenException("403 Forbidden.");
        }
        List<S3Document> s3Documents = documentRepository.findBySourceTypeAndSourceIdAndOperationTypeAndDeletedDateIsNull(
                "USER",
                userId,
                "import");
        if (s3Documents.isEmpty()) {
            return new ArrayList<>();
        }
        List<PackageV2DTO> packageV2DTOS = packageClient.getPackagesList();
        List<DocumentImportDTO> result = s3Documents.stream().flatMap(s3Document -> {
            Optional<PackageV2DTO> matchingPackage = packageV2DTOS.stream()
                    .filter(packagev2 -> s3Document.getId().equals(packagev2.getSourceId()))
                    .findFirst();

            return matchingPackage.map(p -> Stream.of(documentImportMapper.convertToDto(s3Document, p)))
                    .orElseGet(() -> Stream.of(documentImportMapper.convertToDto(s3Document, null)));
        }).sorted(Comparator.comparing(DocumentImportDTO::getCreatedDate).reversed()).collect(Collectors.toList());
        return result;
    }

    public List<DocumentExportDTO> getDocumentsExport(Integer userId) {
        List<S3Document> s3Documents = documentRepository.findBySourceTypeAndSourceIdAndOperationTypeAndDeletedDateIsNull(
                "USER",
                userId,
                "export");
        if (s3Documents.isEmpty()) {
            return new ArrayList<>();
        } else {
            return s3Documents.stream()
                    .map(documentExportMapper::convertToDto)
                    .sorted(Comparator.comparing(DocumentExportDTO::getCreatedDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public List<DocumentVersionDTO> getDocumentVersions(Integer documentationTypeId, Integer targetId) {
        List<S3Document> documents = documentRepository.findByDocumentationTypeIdAndTargetEntityIdOrderByCreatedDateDesc(
                documentationTypeId,
                targetId);

        return documents.stream().map(doc -> new DocumentVersionDTO(doc.getId(), doc.getKey(), doc.getCreatedDate()))
                .collect(Collectors.toList());
    }
}

