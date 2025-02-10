package ru.beeline.documentservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import ru.beeline.documentservice.exception.ForbiddenException;
import ru.beeline.documentservice.exception.NotFoundException;
import ru.beeline.documentservice.exception.S3Exception;
import ru.beeline.documentservice.exception.ValidationException;


@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleException(ForbiddenException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleException(NotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<Object> handleException(S3Exception e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleException(ValidationException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException e) {
        String errorMessage = "Ошибка при загрузке файла, Заголовок: Content-Type должен быть: multipart/form-data; " +
                "boundary=<calculated when request is sent>";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(errorMessage);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleContentTypeException(MissingServletRequestPartException e) {
        String errorMessage = "Заголовок: Content-Type должен быть: multipart/form-data; " +
                "boundary=<calculated when request is sent>";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(errorMessage);
    }
}