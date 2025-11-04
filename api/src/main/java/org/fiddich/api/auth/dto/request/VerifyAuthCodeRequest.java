package org.fiddich.api.auth.dto.request;

import lombok.Getter;

@Getter
public class VerifyAuthCodeRequest {

    private String email;
    private String authCode;
}
