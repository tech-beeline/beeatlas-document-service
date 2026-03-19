/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentExportDTO {
    private Integer id;
    @JsonProperty("doc_type")
    private String docType;
    @JsonProperty("created_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createdDate;
    private String key;
    @JsonProperty("entity_type")
    private String entityType;
    @JsonProperty("operation_type")
    private String operationType;
    private String status;
}
