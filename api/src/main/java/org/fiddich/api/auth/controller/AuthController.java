package org.fiddich.api.auth.controller;

import lombok.RequiredArgsConstructor;
import org.fiddich.api.auth.dto.request.VerifyAuthCodeRequest;
import org.fiddich.api.auth.dto.request.EmailRequest;
import org.fiddich.api.auth.dto.response.ReissueResponse;
import org.fiddich.api.auth.service.AuthService;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(@RequestHeader("refresh") String refreshToken) {
        return ApiResponse.onSuccess(authService.reissueProcess(refreshToken));
    }

    @PostMapping("/mail/send")
    public ApiResponse<Void> sendAuthCode(@RequestBody EmailRequest emailRequest) throws Exception {
        authService.sendAuthCode(emailRequest);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/mail/verify")
    public ApiResponse<Void> verifyAuthCode(@RequestBody VerifyAuthCodeRequest verifyAuthCodeRequest) {
        boolean isValid = authService.verifyAuthCode(verifyAuthCodeRequest);
        if (isValid) {
            return ApiResponse.onSuccess(null);
        } else {
            return ApiResponse.onFailure("CONFLICT", "인증실패");
        }
    }

}
