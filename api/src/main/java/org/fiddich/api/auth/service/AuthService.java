package org.fiddich.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.fiddich.api.auth.dto.EmailDto;
import org.fiddich.api.auth.dto.ReissueResponse;
import org.fiddich.coreinfraemail.EmailUtil;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final EmailUtil emailUtil;

    public ReissueResponse reissueProcess(String refreshToken) {
        return ReissueResponse.from(jwtUtil.reissueToken(refreshToken));
    }

    public String sendAuthCode(EmailDto emailDto) {
        try {
            return emailUtil.sendEmail(emailDto.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    public boolean verifyAuthCode(String email, String authCode) {
        return emailUtil.verifyAuthCode(email, authCode);
//        boolean isValid = emailUtil.verifyAuthCode(email, authCode);
//        if(!isValid) {
//            throw new RuntimeException();
//        }
    }

}
