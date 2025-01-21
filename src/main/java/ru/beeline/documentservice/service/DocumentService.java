package ru.beeline.documentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.exception.ForbiddenException;
import ru.beeline.documentservice.exception.NotFoundException;
import ru.beeline.documentservice.exception.S3Exception;
import ru.beeline.documentservice.repository.DocumentRepository;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private S3Client s3Client;

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
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try {
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return responseBytes.asByteArray();
        } catch (Exception e) {
            throw new S3Exception("Ошибка при загрузке документа");
        }
    }

    private String extractFileNameFromKey(String key) {
        String[] parts = key.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : key;
    }
}


