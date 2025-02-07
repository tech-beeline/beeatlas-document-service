package ru.beeline.documentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.service.DocumentService;

import javax.servlet.http.HttpServletRequest;

import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_ROLES_HEADER;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer id,
                                              @RequestHeader(value = USER_ROLES_HEADER, required = false) String userRoles,
                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId) {
        return documentService.getDocument(id, userRoles, userId);

    }

    @PostMapping("/import/{entityType}")
    public ResponseEntity<DocIdDTO> uploadFileAndStartProcess(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "sync", required = false) boolean sync,
                                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                              @PathVariable String entityType,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadFileAndStartProcess(file,
                sync, userId, entityType, request));
    }

    @PostMapping("/documents/{path_name}/{doc_type}")
    public ResponseEntity<DocIdDTO> uploadExcelFile(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(value = "isPublic", required = false) boolean isPublic,
                                                    @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                    @PathVariable String path_name,
                                                    @PathVariable String doc_type,
                                                    HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.uploadExcelFile(file,
                isPublic, path_name, doc_type, request, userId));
    }
}


