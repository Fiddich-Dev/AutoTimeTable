package org.fiddich.api.auth.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.auth.dto.AuthCodeVerifyDTO;
import org.fiddich.api.auth.service.AuthService;
import org.fiddich.api.domain.member.dto.AuthSchoolDto;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ApiResponse<JWTDto> reissue(@RequestHeader("refresh") String refreshToken) {
        log.info("reissue");
        return ApiResponse.onSuccess(authService.reissueProcess(refreshToken));
    }

    @PostMapping("/mail/send")
    public ApiResponse<Void> sendAuthCode(@RequestBody String email) {
        log.info("sendAuthCode");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(email, new TypeReference<>() {});
            email = map.get("email");

            authService.sendAuthCode(email);
            return ApiResponse.onSuccess(null);
        } catch (Exception e) {
            return ApiResponse.onFailure("INVALID_JSON", "요청 형식이 잘못되었습니다.");
        }
    }

    @PostMapping("/mail/verify")
    public ApiResponse<?> verifyAuthCode(@RequestBody AuthCodeVerifyDTO authCodeVerifyDTO) {
        log.info("verifyAuthCode()");
        boolean isValid = authService.verifyAuthCode(authCodeVerifyDTO.getEmail(), authCodeVerifyDTO.getAuthCode());
        if(isValid) {
            return ApiResponse.onSuccess(null);
        }
        else {
            return ApiResponse.onFailure("403", "인증실패");
        }
    }



}
