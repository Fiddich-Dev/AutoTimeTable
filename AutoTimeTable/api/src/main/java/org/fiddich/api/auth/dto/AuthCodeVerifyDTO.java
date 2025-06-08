package org.fiddich.api.auth.dto;

import lombok.Getter;

@Getter
public class AuthCodeVerifyDTO {

    private String email;
    private String authCode;
}
