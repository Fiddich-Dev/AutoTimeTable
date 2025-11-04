package org.fiddich.api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.auth.dto.request.EmailRequest;
import org.fiddich.api.auth.dto.response.ReissueResponse;
import org.fiddich.coreinfradomain.domain.common.BaseErrorCode;
import org.fiddich.coreinfraemail.EmailUtil;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final EmailUtil emailUtil;

    public ReissueResponse reissueProcess(String refreshToken) {
        log.info("[*] Generate new token pair with {}", refreshToken);
        return ReissueResponse.from(jwtUtil.reissueToken(refreshToken));
    }

    public String sendAuthCode(EmailRequest emailRequest) throws Exception {
        log.info("[*] Send email to {}", emailRequest.getEmail());
        return emailUtil.sendEmail(emailRequest.getEmail());
    }

    public boolean verifyAuthCode(String email, String authCode) {
        log.info("[*] email({}) verify with code {}", email, authCode);
        return emailUtil.verifyAuthCode(email, authCode);
    }

}
