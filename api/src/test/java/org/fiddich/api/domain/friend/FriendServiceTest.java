package org.fiddich.api.domain.friend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.auth.service.AuthService;
import org.fiddich.api.domain.friend.dto.FriendShipDto;
import org.fiddich.api.domain.friend.dto.InquiryMemberDto;
import org.fiddich.api.domain.friend.dto.SearchFriendStatus;
import org.fiddich.api.domain.friend.dto.SearchMemberDto;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.member.dto.JoinDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=salkdhjaslkdjsalkjdaslkjasdlkahskdsahdksahkdhaskdjhsakj",
        "SMTP_PORT=587",
        "SMTP_USERNAME=hiws9997@gmail.com",
        "SMTP_PASSWORD=gedu ihvz eqsg qwtn",
})
class FriendServiceTest {


    private static final Logger log = LoggerFactory.getLogger(FriendServiceTest.class);
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
    @DisplayName(value = "요청 보내고 보류중인 목록 확인")
    void 친구요청보내기() throws Exception {
        // given
        Long myId = join();
        login();
        saveUserDetails(myId);

        // when
        Long friendId = ids.get(0);
        FriendShipDto friendShipDto = new FriendShipDto(friendId);
        friendService.sendFriendRequest(friendShipDto);

        // then
        SecurityContextHolder.clearContext();
        saveUserDetails(friendId);
        List<InquiryMemberDto> pendingFriends = friendService.findPendingResponse();
        Assertions.assertThat(pendingFriends.size()).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "요청 보내고 수락후 친구 목록 확인")
    void 친구요청수락() throws Exception {
        // given
        Long myId = join();
        login();
        saveUserDetails(myId);

        Long friendId = ids.get(0);
        FriendShipDto friendShipDto = new FriendShipDto(friendId);
        friendService.sendFriendRequest(friendShipDto);

        // when
        SecurityContextHolder.clearContext();
        saveUserDetails(ids.get(0));
        friendService.acceptFriendRequest(new FriendShipDto(myId));

        // then
        List<InquiryMemberDto> friendFriends =  friendService.findAllFriends();
        Assertions.assertThat(friendFriends.size()).isEqualTo(1);
        SecurityContextHolder.clearContext();
        saveUserDetails(myId);
        List<InquiryMemberDto> myFriends =  friendService.findAllFriends();
        Assertions.assertThat(myFriends.size()).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "요청 보내고 거절후 친구 목록 확인")
    void 친구요청거절() throws Exception {
        // given
        Long myId = join();
        login();
        saveUserDetails(myId);

        Long friendId = ids.get(0);
        FriendShipDto friendShipDto = new FriendShipDto(friendId);
        friendService.sendFriendRequest(friendShipDto);

        // when
        SecurityContextHolder.clearContext();
        saveUserDetails(ids.get(0));
        friendService.rejectFriendRequest(myId);

        // then
        List<InquiryMemberDto> friendPendingFriends =  friendService.findPendingResponse();
        Assertions.assertThat(friendPendingFriends.size()).isEqualTo(0);
        SecurityContextHolder.clearContext();
        saveUserDetails(myId);
        List<InquiryMemberDto> myPendingFriends =  friendService.findPendingResponse();
        Assertions.assertThat(myPendingFriends.size()).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "친구 삭제")
    void 친구삭제() throws Exception {
        // given
        Long myId = join();
        login();
        saveUserDetails(myId);

        Long friendId = ids.get(0);
        FriendShipDto friendShipDto = new FriendShipDto(friendId);
        friendService.sendFriendRequest(friendShipDto);

        SecurityContextHolder.clearContext();
        saveUserDetails(ids.get(0));
        friendService.acceptFriendRequest(new FriendShipDto(myId));

        // when
        friendService.deleteFriend(myId);

        // then
        List<InquiryMemberDto> friendFriends =  friendService.findAllFriends();
        Assertions.assertThat(friendFriends.size()).isEqualTo(0);
        SecurityContextHolder.clearContext();
        saveUserDetails(myId);
        List<InquiryMemberDto> myFriends =  friendService.findAllFriends();
        Assertions.assertThat(myFriends.size()).isEqualTo(0);
    }

    @Test
    @DisplayName(value = "친구 검색")
    void 친구검색() throws Exception {
        // given
        Long myId = join();
        login();
        saveUserDetails(myId);

        // 보류중인 친구
        Long friendId = ids.get(0);
        FriendShipDto friendShipDto = new FriendShipDto(friendId);
        friendService.sendFriendRequest(friendShipDto);

        // 수락된 친구
        Long friendId1 = ids.get(1);
        FriendShipDto friendShipDto1 = new FriendShipDto(friendId1);
        friendService.sendFriendRequest(friendShipDto1);
        SecurityContextHolder.clearContext();
        saveUserDetails(friendId1);
        friendService.acceptFriendRequest(new FriendShipDto(myId));

        // when
        SecurityContextHolder.clearContext();
        saveUserDetails(myId);
        List<SearchMemberDto> searchMemberDtos = friendService.searchMemberByStudentId("test");

        // then
        Assertions.assertThat(searchMemberDtos.size()).isEqualTo(ids.size());

        List<SearchMemberDto> pendingFriends = searchMemberDtos.stream()
                .filter(s -> s.getStatus() == SearchFriendStatus.PENDING)
                .toList();
        Assertions.assertThat(pendingFriends.size()).isEqualTo(1);

        List<SearchMemberDto> acceptedFriends = searchMemberDtos.stream()
                .filter(s -> s.getStatus() == SearchFriendStatus.ALREADY_FRIEND)
                .toList();
        Assertions.assertThat(acceptedFriends.size()).isEqualTo(1);

        List<SearchMemberDto> notFriends = searchMemberDtos.stream()
                .filter(s -> s.getStatus() == SearchFriendStatus.NOT_FRIEND)
                .toList();
        Assertions.assertThat(notFriends.size()).isEqualTo(ids.size() - pendingFriends.size() - acceptedFriends.size());
    }

}