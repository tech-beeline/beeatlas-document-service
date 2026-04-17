/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.dto.DocumentExportDTO;
import ru.beeline.documentservice.dto.DocumentImportDTO;
import ru.beeline.documentservice.dto.DocumentVersionDTO;
import ru.beeline.documentservice.service.DocumentService;

import java.util.List;

import static ru.beeline.documentservice.utils.Constants.*;

@Tag(name = "Documents", description = "Операции с документами и их версиями")
@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/documents/{id}")
    @Operation(summary = "Получение документа по id")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer id,
                                              @Parameter(description = "Роли пользователя (например, содержит ADMINISTRATOR)", required = false)
                                              @RequestHeader(value = USER_ROLES_HEADER, required = false) String userRoles,
                                              @Parameter(description = "Идентификатор пользователя", required = false)
                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId) {
        return documentService.getDocument(id, userRoles, userId);

    }

    @GetMapping("/documents/import")
    @Operation(summary = "Список импортированных документов (для администратора)")
    public List<DocumentImportDTO> getDocumentsImport(@RequestHeader(value = USER_ID_HEADER) Integer userId) {
        return documentService.getDocumentsImport(userId);
    }

    @GetMapping("/documents/export")
    @Operation(summary = "Список экспортированных документов")
    public List<DocumentExportDTO> getDocumentsExport(@RequestHeader(value = USER_ID_HEADER) Integer userId) {
        return documentService.getDocumentsExport(userId);
    }

    @PostMapping("/import/{entityType}")
    @Operation(summary = "Загрузка файла и старт процесса (Camunda)")
    public ResponseEntity<DocIdDTO> uploadFileAndStartProcess(@RequestPart("file") MultipartFile file,
                                                              @RequestParam(value = "sync", required = false) boolean sync,
                                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                              @PathVariable String entityType,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadFileAndStartProcess(file,
                sync, userId, entityType, request));
    }

    @PostMapping(value = "/documents/{path_name}/{doc_type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузка документа в S3 и сохранение метаданных")
    public ResponseEntity<DocIdDTO> uploadExcelFile(
                                                    @RequestPart("file") MultipartFile file,
                                                    @RequestParam(value = "isPublic", required = false) boolean isPublic,
                                                    @RequestParam(value = "targetId", required = false) Integer targetId,
                                                    @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                    @Parameter(description = "Имя файла (Content-Disposition)", required = true)
                                                    @RequestHeader(value = CONTENT_DISPOSITION, required = true)
                                                    String contentDisposition,
                                                    @PathVariable(name = "path_name") String pathName,
                                                    @PathVariable(name = "doc_type") String docType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadExcelFile(file, isPublic, pathName,
                docType, userId, contentDisposition, targetId));
    }

    @GetMapping("/documents/versions/{documentationsTypeId}/{targetId}")
    @Operation(summary = "Список версий документа по типу документации и targetId")
    public List<DocumentVersionDTO> getDocumentVersions(
            @PathVariable("documentationsTypeId") Integer documentationTypeId,
            @PathVariable("targetId") Integer targetId) {
        return documentService.getDocumentVersions(documentationTypeId, targetId);
    }


    @GetMapping("/documents/{documentationTypeId}/{targetId}")
    @Operation(summary = "Получение последней версии документа по типу и targetId")
    public ResponseEntity<byte[]> getDocumentByTypeAndTarget(@PathVariable Integer documentationTypeId,
                                                             @PathVariable Integer targetId,
                                                             @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                             @RequestHeader(value = USER_ROLES_HEADER, required = false) String userRoles) {
        return documentService.getDocumentByTypeAndTarget(documentationTypeId, targetId, userId, userRoles);
    }

    @PatchMapping("/export/{doc_id}")
    @Operation(summary = "Дозагрузка (reload) документа по docId")
    public ResponseEntity patchCapabilityMap(@PathVariable(name = "doc_id") Integer docId,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestHeader(value = CONTENT_DISPOSITION, required = false)
                                             String contentDisposition) {
        documentService.documentReloading(docId, file, contentDisposition);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/export/{entity_type}")
    @Operation(summary = "Асинхронная выгрузка документа")
    public ResponseEntity<DocIdDTO> postAsynchronousDocumentLoading(@PathVariable(name = "entity_type") String entityType,
                                                                    @RequestHeader(value = USER_ID_HEADER) Integer userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.asynchronousDocumentLoading(entityType, userId));
    }

    @DeleteMapping("/documents")
    @Operation(summary = "Удаление устаревших документов")
    public ResponseEntity<Void> deleteDocuments (){
        documentService.deleteDocuments();
        return new  ResponseEntity<>(HttpStatus.OK);
    }
}





