package org.fiddich.coreinfrasecurity.jwt.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.jwt.exception.SecurityCustomException;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.fiddich.coreinfrasecurity.jwt.exception.SecurityErrorCode.INVALID_TOKEN;
import static org.fiddich.coreinfrasecurity.jwt.exception.SecurityErrorCode.TOKEN_EXPIRED;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final RedisUtil redisUtil;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret, RedisUtil redisUtil) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisUtil = redisUtil;
    }


    public JWTDto reissueToken(String refreshToken) {
        try {
            // 토큰이 비어있는지 확인
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalArgumentException();
            }

            String studentId = getStudentId(refreshToken);
            Long id = getId(refreshToken);
            String role = getRole(refreshToken);

            // 토큰이 redis에 있는지 확인
            List<String> refreshTokens = redisUtil.findAllValues(studentId + ":refreshToken", 0, -1)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();

            boolean isExist = refreshTokens.contains(refreshToken);
            if (!isExist) {
                throw new NoSuchElementException("리프레시 토큰이 만료되었습니다. 다시 로그인 해주세요.");
            }

            // 새로운 access, refresh 토큰 재발급
            String newAccessToken = createJwt("access", id, studentId, role, 600000L);
            String newRefreshToken = createJwt("refresh", id, studentId, role, 86400000L);

            // redis 리이슈 하는데 사용한 refresh토큰 삭제
            // 새로 받은 refresh 토큰 redis에 저장
            // 만료기간 7일로 갱신
            redisUtil.deleteOneValue(studentId + ":refreshToken", refreshToken);
            redisUtil.addOneValue(studentId + ":refreshToken", newRefreshToken);
            redisUtil.updateExpirationTime(studentId + ":refreshToken", 7L, TimeUnit.DAYS);

            return new JWTDto(newAccessToken, newRefreshToken);
        } catch (IllegalArgumentException iae) {
            throw new SecurityCustomException(INVALID_TOKEN, iae);
        } catch (ExpiredJwtException eje) {
            throw new SecurityCustomException(TOKEN_EXPIRED, eje);
        }
    }


    public Long getId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    public String getStudentId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("studentId", String.class);
    }

    public String getSchool(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("school", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }


    // 카테고리, 학번, 권한, 유효기간으로 토큰을 생성
    public String createJwt(String category, Long id, String studentId, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("id", id) // 페이로드에 Key, Value로 데이터를 넣음
                .claim("studentId", studentId)
                .claim("category", category)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // 토큰 생성시간 정보
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 토큰 만료시간 정보
                .signWith(secretKey)
                .compact();
    }
}
