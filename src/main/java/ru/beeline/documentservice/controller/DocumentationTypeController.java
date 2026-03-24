/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.beeline.documentservice.dto.*;
import ru.beeline.documentservice.service.DocumentationTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentations")
public class DocumentationTypeController {

    @Autowired
    private DocumentationTypeService documentationTypeService;

    @GetMapping("/{entity-type}")
    @ApiOperation(value = "Получения типа документа по типу сущности")
    public List<DocumentationTypeDTO> getDocumentationTypeByEntityType(@PathVariable(name = "entity-type") String entityType) {

        return documentationTypeService.getDocumentationTypeByEntityType(entityType);
    }

}


