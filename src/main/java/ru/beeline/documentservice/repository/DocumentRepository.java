/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.beeline.documentservice.domain.S3Document;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<S3Document, Integer> {

    List<S3Document> findBySourceTypeAndSourceIdAndOperationTypeAndDeletedDateIsNull(String sourceType, Integer sourceId,
                                                                                     String operationType);

    @Query("SELECT d FROM S3Document d " +
            "WHERE d.documentationType.id = :documentationTypeId " +
            "AND d.targetEntityId = :targetId " +
            "ORDER BY d.createdDate DESC")
    List<S3Document> findByDocumentationTypeIdAndTargetEntityIdOrderByCreatedDateDesc(
            @Param("documentationTypeId") Integer documentationTypeId,
            @Param("targetId") Integer targetId
    );

    Optional<S3Document> findTopByDocumentationTypeIdAndTargetEntityIdOrderByCreatedDateDesc(Integer documentationTypeId,
                                                                                             Integer targetEntityId);

    boolean existsByKey(String key);

    @Query("SELECT d FROM S3Document d" +
            " WHERE d.ttl != 0 AND d.deletedDate IS NULL" +
            " AND (CURRENT_DATE - CAST(d.createdDate AS date)) > d.ttl")
    List<S3Document> findAllNotDocumentation();
}
