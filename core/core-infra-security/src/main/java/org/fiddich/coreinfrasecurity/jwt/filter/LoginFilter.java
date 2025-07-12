package org.fiddich.coreinfrasecurity.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.Lecture.School;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.util.HttpResponseUtil;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    // 로그인 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("attemptAuthentication()");

        //클라이언트 요청에서 studentId, password 추출
        // Java 객체를 JSON 문자열로 변환 (Serialization),
        Map<String, Object> requestBody;
        try {
            requestBody = getBody(request);
            String studentId = (String)requestBody.get("studentId");
            // 암호화 되기전 비밀번호
            String password = (String)requestBody.get("password");

            log.info("studentId = {}, password = {}", studentId, password);

            //스프링 시큐리티에서 studentId와 password를 검증하기 위해서는 token에 담아야 함
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(studentId, password, null);

            //token에 담은 검증을 위한 AuthenticationManager로 전달
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Error occurred while parsing request body");
        }

    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        // jwtFilter 마지막에 컨텍스트 홀더에 저장한 값을 가져온다
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        // 학번과 권한을 가져온다
        String studentId = customUserDetails.getStudentId();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();
        Long id = customUserDetails.getId();

        // 토큰 생성
        String access = jwtUtil.createJwt("access", id, studentId, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", id, studentId, role, 86400000L);


        // redis에 refresh토큰만 저장
        redisUtil.addOneValue( studentId + ":refreshToken", refresh);
        redisUtil.updateExpirationTime(studentId + ":refreshToken", 7L, TimeUnit.DAYS);

        JWTDto jwtDto = new JWTDto(access, refresh);

        HttpResponseUtil.setSuccessResponse(response, HttpStatus.CREATED, jwtDto);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("unsuccessfulAuthentication()");
        HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.UNAUTHORIZED.name(), "아이디 혹은 비밀번호가 일치하지 않습니다"));
    }

    private Map<String, Object> getBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try (BufferedReader bufferedReader = request.getReader()) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        String requestBody = stringBuilder.toString();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(requestBody, Map.class);
    }

}
