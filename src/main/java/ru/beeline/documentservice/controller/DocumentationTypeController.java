/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.beeline.documentservice.dto.DocumentationTypeDTO;
import ru.beeline.documentservice.service.DocumentationTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentations")
@Tag(name = "Типы документации", description = "Справочник типов документации по сущностям")
public class DocumentationTypeController {

    @Autowired
    private DocumentationTypeService documentationTypeService;

    @GetMapping("/{entity-type}")
    @Operation(summary = "Получение типа документации по типу сущности")
    public List<DocumentationTypeDTO> getDocumentationTypeByEntityType(@PathVariable(name = "entity-type") String entityType) {

        return documentationTypeService.getDocumentationTypeByEntityType(entityType);
    }
}


