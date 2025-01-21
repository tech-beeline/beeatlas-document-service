package ru.beeline.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.beeline.documentservice.domain.S3Document;

public interface DocumentRepository extends JpaRepository<S3Document, Integer> {
}
