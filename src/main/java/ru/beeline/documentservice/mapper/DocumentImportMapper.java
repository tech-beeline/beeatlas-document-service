package ru.beeline.documentservice.mapper;

import org.springframework.stereotype.Component;
import ru.beeline.documentservice.domain.S3Document;
import ru.beeline.documentservice.dto.DocumentImportDTO;
import ru.beeline.documentservice.dto.PackageV2DTO;

@Component
public class DocumentImportMapper {

    public DocumentImportDTO convertToDto(S3Document s3Document, PackageV2DTO packageDTO) {
        PackageV2DTO packageV2 = PackageV2DTO.builder()
                .allParts(packageDTO.getAllParts())
                .createdDate(packageDTO.getCreatedDate())
                .errorParts(packageDTO.getErrorParts())
                .operation(packageDTO.getOperation())
                .packageId(packageDTO.getPackageId())
                .processParts(packageDTO.getProcessParts())
                .status(packageDTO.getStatus())
                .successParts(packageDTO.getSuccessParts())
                .source(packageDTO.getSource())
                .sourceId(packageDTO.getSourceId())
                .build();
        return DocumentImportDTO.builder()
                .id(s3Document.getId())
                .docType(s3Document.getDocType())
                .createdDate(s3Document.getCreatedDate())
                .key(s3Document.getKey())
                .entityType(s3Document.getEntityType())
                .operationType(s3Document.getOperationType())
                .packageInfo(packageDTO == null ? null : packageV2)
                .build();
    }
}
