package org.fiddich.api.domain.member;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.friend.FriendService;
import org.fiddich.api.domain.member.dto.request.SignUpRequest;
import org.fiddich.api.domain.member.dto.request.PasswordRequest;
import org.fiddich.api.domain.member.dto.request.PasswordResetRequest;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
class MemberServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FriendService friendService;
    @Autowired
    BCryptPasswordEncoder encoder;

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
        SignUpRequest signUpRequest = new SignUpRequest("내학번", "내비밀번호", "내이름");
        Long id = memberService.join(signUpRequest);
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
    @DisplayName("회원가입후 학번 중복확인")
    public void 학번중복확인1() {
        // given
        Long myId = join();
        Member me = memberRepository.findById(myId).get();

        // when
        boolean isDuplicate = memberService.isDuplicatedMember(me.getStudentId());

        // then
        Assertions.assertThat(isDuplicate).isTrue();
    }

    @Test
    @DisplayName("탈퇴후 학번 중복확인")
    public void 학번중복확인2() {
        // given
        Long myId = join();
        Member me = memberRepository.findById(myId).get();
        saveUserDetails(myId);

        // when
        memberService.withdrawal();

        // then
        boolean isDuplicate = memberService.isDuplicatedMember(me.getStudentId());
        Assertions.assertThat(isDuplicate).isFalse();
    }

    @Test
    @DisplayName("비밀번호 초기화")
    public void 비밀번호초기화() throws Exception {
        // given
        Long myId = join();
        Member me = memberRepository.findById(myId).get();
        saveUserDetails(myId);

        // when
        String newPassword = "new-password";
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(me.getStudentId(), newPassword);
        memberService.resetPassword(passwordResetRequest);

        // then
        String requestBody = """
    {
        "studentId": "%s",
        "password": "%s"
    }
    """.formatted(me.getStudentId(), newPassword);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()) // 로그인 성공 시 201
                .andExpect(jsonPath("$.content.access").exists())
                .andExpect(jsonPath("$.content.refresh").exists());
    }

    @Test
    @DisplayName("비밀번호 변경")
    void 비밀번호변경() throws Exception {
        // given
        Long myId = join();
        Member me = memberRepository.findById(myId).get();
        saveUserDetails(myId);

        // when
        PasswordRequest passwordRequest = new PasswordRequest("틀린비밀번호");
        Assertions.assertThatThrownBy(() -> {
            throw new IllegalArgumentException();
        });

        passwordRequest = new PasswordRequest("내비밀번호");
        memberService.validPassword(passwordRequest);
        passwordRequest = new PasswordRequest("새비밀번호");
        memberService.changePassword(passwordRequest);

        em.flush();
        em.clear();

        // then
        Assertions.assertThat(encoder.matches("새비밀번호", me.getPassword())).isTrue();
    }
}