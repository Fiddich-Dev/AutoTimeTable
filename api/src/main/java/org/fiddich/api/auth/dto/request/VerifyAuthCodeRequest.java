package org.fiddich.api.auth.dto.request;

public record VerifyAuthCodeRequest(
        String email,
        String authCode
) {}
