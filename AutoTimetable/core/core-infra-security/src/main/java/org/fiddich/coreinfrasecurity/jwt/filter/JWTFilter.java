package org.fiddich.coreinfrasecurity.jwt.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfrasecurity.jwt.util.HttpResponseUtil;
import org.fiddich.coreinfrasecurity.jwt.util.JWTUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("Authorization");

        // 토큰이 없다면 다음 필터로 넘김
        if(accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 있으면 "bearer "부분 제거
        accessToken = accessToken.substring(7);

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            // 클라랑 소통해서 상태메시지 통일해야함
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.BAD_REQUEST.name(), "만료된 access 토큰"));
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);
        // 만약 입력받은 토큰이 access토큰이 아니면
        if (!category.equals("access")) {
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.onFailure(HttpStatus.BAD_REQUEST.name(), "access 토큰 아님"));
            return;
        }
        // 학번과 권한 조회
        // 권한은 앞에 "ROLE_" 붙여줘야함
        Long id = jwtUtil.getId(accessToken);
        String studentId = jwtUtil.getStudentId(accessToken);
        String school = jwtUtil.getSchool(accessToken);
        String role = "ROLE_" + jwtUtil.getRole(accessToken);

        // 컨텍스트 홀더에 저장될 객체 생성
        Member member = Member.builder()
                .id(id)
                .studentId(studentId)
//                .school(school)
                .role(role)
                .build();
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 컨텍스트 홀더에 저장
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

}
