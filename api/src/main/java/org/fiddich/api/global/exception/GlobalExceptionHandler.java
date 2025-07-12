package org.fiddich.api.global.exception;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> duplicateKeyException(DuplicateKeyException e) {
        log.warn(">>>>> DuplicateKeyException Error : ", e);
        return ResponseEntity.status(HttpStatus.CONFLICT.value()).body(ApiResponse.onFailure(HttpStatus.CONFLICT.name(), e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<?>> handleAllException(NoSuchElementException e) {
        log.warn(">>>>> NoSuchElementException Error : ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(ApiResponse.onFailure(HttpStatus.UNAUTHORIZED.name(), e.getMessage()));
    }


}
