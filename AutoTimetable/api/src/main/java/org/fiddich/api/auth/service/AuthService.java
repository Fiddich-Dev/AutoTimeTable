package org.fiddich.api.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
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
    private final JavaMailSender mailSender;


    public JWTDto reissueProcess(String refreshToken) {

        // 토큰이 비어있는지 확인
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        String studentId = jwtUtil.getStudentId(refreshToken);
        Long id = jwtUtil.getId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String school = jwtUtil.getSchool(refreshToken);

        // 토큰이 redis에 있는지 확인
        List<String> refreshTokens = redisUtil.findAllValues(SchoolNameConverter.convertToEng(school) + ":" + studentId + ":refreshToken", 0, -1)
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        Boolean isExist = refreshTokens.contains(refreshToken);
        if (!isExist) {
            throw new NoSuchElementException("리프레시 토큰이 만료되었습니다. 다시 로그인 해주세요.");
        }

        // 새로운 access, refresh 토큰 재발급
        String newAccessToken = jwtUtil.createJwt("access", id, studentId, school, role, 600000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", id, studentId, school, role, 86400000L);

        // redis 리이슈 하는데 사용한 refresh토큰 삭제
        // 새로 받은 refresh 토큰 redis에 저장
        // 만료기간 7일로 갱신
        redisUtil.deleteOneValue(SchoolNameConverter.convertToEng(school) + ":" + studentId + ":refreshToken", refreshToken);
        redisUtil.addOneValue(SchoolNameConverter.convertToEng(school) + ":" + studentId + ":refreshToken", newRefreshToken);
        redisUtil.updateExpirationTime(SchoolNameConverter.convertToEng(school) + ":" + studentId + ":refreshToken", 7L, TimeUnit.DAYS);

        return new JWTDto(newAccessToken, newRefreshToken);
    }

    public String sendAuthCode(String email) {
        String authCode = generateAuthCode();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email);
            System.out.println(email);
            helper.setSubject("이메일 인증 코드");
            helper.setText("<h3>인증 코드: <strong>" + authCode + "</strong></h3>", true); // HTML 형식

            mailSender.send(message);
            // redis에 저장
            redisUtil.saveAsValue(email, authCode, 5L, TimeUnit.MINUTES);
            return authCode;
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    public boolean verifyAuthCode(String email, String authCode) {
        if(authCode.equals(redisUtil.getValue(email))) {
            redisUtil.deleteKey(email);
            return true;
        }
        return false;
    }


    // 인증번호 생성 메소드
    public String generateAuthCode() {
        Random random = new Random();
        int authCode = 100000 + random.nextInt(900000); // 6자리 난수 생성
        return String.valueOf(authCode);
    }
}
