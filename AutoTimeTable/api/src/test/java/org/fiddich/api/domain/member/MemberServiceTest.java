package org.fiddich.api.domain.member;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.member.dto.JoinDto;
import org.fiddich.api.domain.member.dto.MemberIdentifierDto;
import org.fiddich.api.domain.member.dto.RequestFriendshipDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    EntityManager em;

    List<Long> ids = new ArrayList<>();

    @BeforeEach
    public void init() {
        for (int i = 1; i <= 5; i++) {
            Member member = Member.builder()
                    .studentId("testStudentId" + i)
                    .password("testPassword" + i)
                    .username("testUsername" + i)
                    .school("testSchool" + i)
                    .department("testDepartment" + i)
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

    public Long join() {
        JoinDto joinDto = new JoinDto("testStudentId", "test비밀번호", "test이름", "성균관대학교", "test학과");
        Long id = memberService.join(joinDto);
        return id;
    }

    public JWTDto login() throws Exception {
        String requestBody = """
        {
            "school": "성균관대학교",
            "studentId": "testStudentId",
            "password": "test비밀번호"
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
    void 회원가입_성공() {
        // given
        JoinDto joinDto = new JoinDto("test학번", "test비밀번호", "test이름", "test학교", "test학과");

        // when
        Long savedId = memberService.join(joinDto);

        // then
        Optional<Member> findMember = memberRepository.findById(savedId);
        assertTrue(findMember.isPresent());
        Assertions.assertThat(findMember.get().getUsername()).isEqualTo(joinDto.getUsername());
    }

    @Test
    void 중복_회원가입() {
        // given
        JoinDto joinDto1 = new JoinDto("test학번", "test비밀번호", "test이름", "test학교", "test학과");
        JoinDto joinDto2 = new JoinDto("test학번", "test비밀번호", "test이름", "test학교", "test학과");

        // when
        Long savedId1 = memberService.join(joinDto1);

        // then
        assertThrows(DuplicateKeyException.class, () -> memberService.join(joinDto2));
    }

    @Test
    void 회원중복체크() {
        // given
        JoinDto joinDto = new JoinDto("test학번", "test비밀번호", "test이름", "test학교", "test학과");
        MemberIdentifierDto dto = new MemberIdentifierDto(joinDto.getStudentId(), joinDto.getSchool());


        // when
        Assertions.assertThat(memberService.isDuplicatedMember(dto)).isEqualTo(false);
        memberService.join(joinDto);
        Assertions.assertThat(memberService.isDuplicatedMember(dto)).isEqualTo(true);
    }

    @Test
    void 로그인() throws Exception {
        // given
        JoinDto joinDto = new JoinDto("testStudentId", "test비밀번호", "test이름", "성균관대학교", "test학과");
        join();
        JWTDto jwt = login();

        // then
        // redis 확인
        String key = SchoolNameConverter.convertToEng(joinDto.getSchool()) + ":" + joinDto.getStudentId() + ":" + "refreshToken";
        List<String> refreshTokens = redisUtil.findAllValues(key, 0, -1).stream().map(Object::toString).collect(Collectors.toList());
        boolean isExist = refreshTokens.contains(jwt.getRefresh());

        Assertions.assertThat(isExist).isEqualTo(true);
        redisUtil.deleteKey(key);
    }

    @Test
    void 회원탈퇴() throws Exception {
        Long id = join();
        saveUserDetails(id);

        Assertions.assertThat(memberRepository.findById(id).isPresent()).isEqualTo(true);

        memberService.withdrawal();

        Assertions.assertThat(memberRepository.findById(id).isPresent()).isEqualTo(false);
    }

    @Test
    void 친구요청보내기() {
        // given
        Long id = join();
        saveUserDetails(id);

        Long receiverId = ids.getFirst();
        RequestFriendshipDto requestFriendshipDto = new RequestFriendshipDto(receiverId);

        // when
        memberService.sendFriendRequest(requestFriendshipDto);

        // then
        // 요청 보낸 member
        Member requester = memberRepository.findById(id).get();
        // receiver한테 요청 보낸 member들
        List<Member> requesters = memberRepository.findById(receiverId).get().getPendingFriends();
        boolean isExist = requesters.contains(requester);
        Assertions.assertThat(isExist).isEqualTo(true);
    }


}