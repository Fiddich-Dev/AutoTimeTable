package org.fiddich.api.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.QueryCounter;
import org.fiddich.api.auth.dto.EmailDto;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.member.dto.JoinDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=aslkdjaslkdjsalkdjaslkdjsaldkjaslkdjass",
        "SMTP_PORT=587",
        "SMTP_USERNAME=schedule.ssku@gmail.com",
        "SMTP_PASSWORD=tmus mpkv ppgq jtxn",
})
class AuthServiceTest {

    @Autowired
    AuthService authService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    EntityManager em;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    List<Long> ids = new ArrayList<>();

    // 테스트 회원들
    @BeforeEach
    public void init() {
        for (int i = 1; i <= 5; i++) {
            Member member = Member.builder()
                    .studentId("testStudentId" + i)
                    .password("testPassword" + i)
                    .username("testUsername" + i)
                    .build();

            em.persist(member);
            ids.add(member.getId());
        }
        em.flush();
        em.clear();
    }


    @AfterEach
    void emptySecurotyContext() {
        // 테스트 후 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    // 나 회원가입
    public Long join() {
        JoinDto joinDto = new JoinDto("내학번", "내비밀번호", "내이름");
        Long id = memberService.join(joinDto);
        return id;
    }

    public JWTDto login() throws Exception {
        String requestBody = """
        {
            "studentId": "내학번",
            "password": "내비밀번호"
        }
        """;

        // when
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content.access").exists())  // content 내부에 access 토큰 확인
                .andExpect(jsonPath("$.content.refresh").exists()) // content 내부에 refresh 토큰 확인
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        Map<String, String> contentMap = (Map<String, String>) map.get("content");

        String access = contentMap.get("access");
        String refresh = contentMap.get("refresh");

        return new JWTDto(access, refresh);
    }

    public void saveUserDetails(Long id) {
        Member member = memberRepository.findById(id).get();

        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void 리이슈() throws Exception {
        Long id = join();
        System.out.println(id);

        JWTDto jwtDto = login();
        saveUserDetails(id);


        Member member = memberRepository.findById(id).get();

        Thread.sleep(1000);

        JWTDto newJwtDto = authService.reissueProcess(jwtDto.getRefresh());

        String key = member.getStudentId() + ":refreshToken";
        boolean isExistOldRefresh = redisUtil.findAllValues(key, 0, -1).contains(jwtDto.getRefresh());
        boolean isExistNewRefresh = redisUtil.findAllValues(key, 0, -1).contains(newJwtDto.getRefresh());

        Assertions.assertThat(isExistOldRefresh).isFalse();
        Assertions.assertThat(isExistNewRefresh).isTrue();

        redisUtil.deleteKey(key);
    }

    @Test
    void 인증번호보내고확인() {
        String email = "hiws99@naver.com";
        String authCode = authService.sendAuthCode(new EmailDto(email));
        boolean isExist = redisUtil.getValue(email).equals(authCode);
        Assertions.assertThat(authService.verifyAuthCode(email, authCode)).isTrue();
        Assertions.assertThat(isExist).isTrue();
    }




}