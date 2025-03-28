package ru.beeline.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.beeline.documentservice.domain.S3Document;

import java.util.List;

public interface DocumentRepository extends JpaRepository<S3Document, Integer> {

    List<S3Document> findBySourceTypeAndSourceIdAndOperationTypeAndDeletedDateIsNull(String sourceType, Integer sourceId,
                                                                                     String operationType);
}
