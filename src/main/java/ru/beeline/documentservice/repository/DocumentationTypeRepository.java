package ru.beeline.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.beeline.documentservice.domain.DocumentationType;

import java.util.Optional;

public interface DocumentationTypeRepository extends JpaRepository<DocumentationType, Integer> {
    Optional<DocumentationType> findByFolder(String folder);
}
