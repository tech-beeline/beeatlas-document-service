/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.domain;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "s3_doc", schema = "documents")
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "key")
    private String key;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private Integer sourceId;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "target_entity_id")
    private Integer targetEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentation_type_id")
    private DocumentationType documentationType;
}
