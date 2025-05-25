package org.fiddich.coreinfrasecurity.jwt.filter;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.util.HttpResponseUtil;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // logout요청이 들어오면
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 그 요청이 post요청이면
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("로그아웃 요청임");

        // 헤더에서 "refresh"로 리프레쉬 토큰 가져오기
        String refresh = request.getHeader("refresh");

        // refresh토큰을 못가져오면 400에러
        if (refresh == null) {
            // HTTP 응답을 즉시 클라이언트에게 전송
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.BAD_REQUEST.name(), "refresh토큰 못가져옴"));
            return;
        }

        // 입력받은 refresh 토큰의 만료시간을 보고 만료되었으면 400에러
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.BAD_REQUEST.name(), "만료된 refresh 토큰"));
            return;
        }

        // 입력받은 토큰의 카테고리가 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.BAD_REQUEST.name(), "refresh토큰이 아님"));
            return;
        }

        // refresh 토큰에서 학번조회
        String studentId = jwtUtil.getStudentId(refresh);

        // redis에 저장되어 있는지 확인
        // 학번:refreshToken을 key로 갖는 모든 리프레쉬 토큰 조회
        List<String> refreshTokens = redisUtil.findAllValues(studentId + ":refreshToken", 0, -1)
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 입력받은 토큰이 redis에 포함되어있는지 확인
        Boolean isExist = refreshTokens.contains(refresh);
        // 없으면 400에러
        if(!isExist) {
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.UNAUTHORIZED.name(), "redis에 일치하는 refresh토큰이 없음"));
            return;
        }

        // 로그아웃 진행
        // redis에서 refreshtoken 제거
        redisUtil.deleteOneValue(studentId + ":refreshToken", refresh);

        HttpResponseUtil.setSuccessResponse(response, HttpStatus.OK, "로그아웃 성공");
    }

}
