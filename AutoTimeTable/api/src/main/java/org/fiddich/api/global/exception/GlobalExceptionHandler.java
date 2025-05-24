package org.fiddich.api.global.exception;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> duplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        log.warn(">>>>> DuplicateKeyException Error : ", e);
        return ResponseEntity.status(HttpStatus.CONFLICT.value()).body(ApiResponse.onFailure(HttpStatus.CONFLICT.name(), e.getMessage()));
    }


}
