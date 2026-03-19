/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.dto.DocumentExportDTO;

@Component
public class DocumentExportMapper {
    public DocumentExportDTO convertToDto(S3Document s3Document) {
        return DocumentExportDTO.builder()
                .id(s3Document.getId())
                .docType(s3Document.getDocType())
                .createdDate(s3Document.getCreatedDate())
                .key(s3Document.getKey())
                .entityType(s3Document.getEntityType())
                .operationType(s3Document.getOperationType())
                .status(s3Document.getKey() != null ? "Документ сформирован" : "Документ подготавливается")
                .build();
    }
}
