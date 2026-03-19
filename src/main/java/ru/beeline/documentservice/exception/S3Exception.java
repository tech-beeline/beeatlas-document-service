/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.exception;

public class S3Exception extends RuntimeException {
    public S3Exception(String message) {
        super(message);
    }
}
