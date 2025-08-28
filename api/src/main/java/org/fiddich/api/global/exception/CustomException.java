package org.fiddich.api.global.exception;

import lombok.Getter;
import org.fiddich.coreinfradomain.domain.common.BaseErrorCode;

@Getter
public class CustomException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
