package ru.beeline.documentservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.dto.DocumentImportDTO;
import ru.beeline.documentservice.service.DocumentService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.beeline.documentservice.utils.Constants.CONTENT_DISPOSITION;
import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_ROLES_HEADER;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/documents/{id}")
    @ApiOperation(value = "Получение документа")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer id,
                                              @RequestHeader(value = USER_ROLES_HEADER, required = false) String userRoles,
                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId) {
        return documentService.getDocument(id, userRoles, userId);

    }

    @GetMapping("/documents/import")
    @ApiOperation(value = "Получения списка документов со связанными пакетами")
    public List<DocumentImportDTO> getDocumentsImport(@RequestHeader(value = USER_ID_HEADER) Integer userId) {
        return documentService.getDocumentsImport(userId);
    }

    @PostMapping("/import/{entityType}")
    @ApiOperation(value = "Загрузка документа и старт процесса")
    public ResponseEntity<DocIdDTO> uploadFileAndStartProcess(@RequestPart("file") MultipartFile file,
                                                              @RequestParam(value = "sync", required = false) boolean sync,
                                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                              @PathVariable String entityType,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadFileAndStartProcess(file,
                sync, userId, entityType, request));
    }

    @PostMapping("/documents/{path_name}/{doc_type}")
    @ApiOperation(value = "Загрузка документов")
    public ResponseEntity<DocIdDTO> uploadExcelFile(@ApiParam(value = "File to upload", required = true) @RequestPart("file") MultipartFile file,
                                                    @RequestParam(value = "isPublic", required = false) boolean isPublic,
                                                    @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                    @RequestHeader(value = CONTENT_DISPOSITION, required = false)
                                                    @ApiParam(value = "Content-Disposition header") String contentDisposition,
                                                    @PathVariable(name = "path_name") String pathName,
                                                    @PathVariable(name = "doc_type") String docType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadExcelFile(file,
                isPublic, pathName, docType, userId, contentDisposition));
    }

    @PatchMapping("/export/{doc_id}")
    @ApiOperation(value = "Дозагрузка документов")
    public ResponseEntity patchCapabilityMap(@PathVariable(name = "doc_id") Integer docId,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestHeader(value = CONTENT_DISPOSITION, required = false)
                                             @ApiParam(value = "Content-Disposition header") String contentDisposition) {
        documentService.documentReloading(docId, file, contentDisposition);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/export/{entity_type}")
    @ApiOperation(value = "")
    public ResponseEntity<DocIdDTO> postAsynchronousDocumentLoading(@PathVariable(name = "entity_type") String entityType,
                                                                    @RequestHeader(value = USER_ID_HEADER) Integer userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.asynchronousDocumentLoading(entityType, userId));
    }
}


