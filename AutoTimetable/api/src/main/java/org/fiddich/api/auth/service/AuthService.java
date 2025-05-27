package org.fiddich.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
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
}
