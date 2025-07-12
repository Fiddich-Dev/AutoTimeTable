package org.fiddich.api.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.fiddich.api.auth.dto.EmailDto;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfraemail.EmailUtil;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final EmailUtil emailUtil;


    public JWTDto reissueProcess(String refreshToken) {

        // 토큰이 비어있는지 확인
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        String studentId = jwtUtil.getStudentId(refreshToken);
        Long id = jwtUtil.getId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
//        String school = jwtUtil.getSchool(refreshToken);

        // 토큰이 redis에 있는지 확인
        List<String> refreshTokens = redisUtil.findAllValues(studentId + ":refreshToken", 0, -1)
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        Boolean isExist = refreshTokens.contains(refreshToken);
        if (!isExist) {
            throw new NoSuchElementException("리프레시 토큰이 만료되었습니다. 다시 로그인 해주세요.");
        }

        // 새로운 access, refresh 토큰 재발급
        String newAccessToken = jwtUtil.createJwt("access", id, studentId, role, 600000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", id, studentId, role, 86400000L);

        // redis 리이슈 하는데 사용한 refresh토큰 삭제
        // 새로 받은 refresh 토큰 redis에 저장
        // 만료기간 7일로 갱신
        redisUtil.deleteOneValue(studentId + ":refreshToken", refreshToken);
        redisUtil.addOneValue(studentId + ":refreshToken", newRefreshToken);
        redisUtil.updateExpirationTime(studentId + ":refreshToken", 7L, TimeUnit.DAYS);

        return new JWTDto(newAccessToken, newRefreshToken);
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
    }

}
