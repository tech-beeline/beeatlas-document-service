package ru.beeline.documentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.documentservice.service.DocumentService;

import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_ROLES_HEADER;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer id,
                                              @RequestHeader(value = USER_ROLES_HEADER, required = false) String userRoles,
                                              @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId) {
        return documentService.getDocument(id, userRoles, userId);

    }
}



