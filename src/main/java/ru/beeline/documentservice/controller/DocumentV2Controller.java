/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.beeline.documentservice.dto.DocIdDTO;
import ru.beeline.documentservice.exception.ValidationException;
import ru.beeline.documentservice.service.DocumentService;

import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;

@RestController
@RequestMapping("/api/v2")
public class DocumentV2Controller {

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/documents/{path_name}/{doc_type}", consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.ALL_VALUE})
    @Operation(summary = "Загрузка документа (raw binary body)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Validation error"), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "503", description = "S3 error")})
    public ResponseEntity<DocIdDTO> uploadBinaryFile(@RequestBody byte[] body,
                                                     @RequestHeader(name = "Content-Length", required = true) @Parameter(description = "Content-Length header", required = true) Long contentLength,
                                                     @RequestHeader(name = "Content-Type", required = false) String contentType,
                                                     @RequestParam(name = "fileName", required = true) @Parameter(description = "Original file name with extension", required = true) String fileName,
                                                     @RequestParam(name = "isPublic", required = false) Boolean isPublic,
                                                     @RequestParam(name = "targetId", required = false) Integer targetId,
                                                     @RequestHeader(value = USER_ID_HEADER, required = false) Integer userId,
                                                     @PathVariable(name = "path_name") String pathName,
                                                     @PathVariable(name = "doc_type") String docType) {
        if (contentLength == null || contentLength <= 0) {
            throw new ValidationException("Отсутствует или некорректный заголовок Content-Length");
        }
        if (body == null) {
            throw new ValidationException("Файл отсутствует или пуст");
        }
        if (contentLength.longValue() != body.length) {
            throw new ValidationException("Content-Length не соответствует размеру тела запроса");
        }
        DocIdDTO result = documentService.uploadBinaryFile(body,
                                                           isPublic != null ? isPublic : false,
                                                           pathName,
                                                           docType,
                                                           userId,
                                                           fileName,
                                                           targetId,
                                                           contentType);
        return ResponseEntity.ok(result);
    }
}

