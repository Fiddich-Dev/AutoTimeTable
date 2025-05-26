package org.fiddich.api.auth.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.auth.service.AuthService;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

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

}
