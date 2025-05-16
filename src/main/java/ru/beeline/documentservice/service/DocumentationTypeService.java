package ru.beeline.documentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.beeline.documentservice.domain.DocumentationType;
import ru.beeline.documentservice.dto.DocumentationTypeDTO;
import ru.beeline.documentservice.repository.DocumentationTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentationTypeService {

    @Autowired
    private DocumentationTypeRepository documentationTypeRepository;

    public List<DocumentationTypeDTO> getDocumentationTypeByEntityType(String entityType) {
        List<DocumentationType> documents = documentationTypeRepository.findByTargetEntityType(entityType);
        return documents.stream()
                .map(doc -> new DocumentationTypeDTO(doc.getId(), doc.getName(), doc.getDocType()))
                .collect(Collectors.toList());
    }
}

