package org.fiddich.api.domain.member;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.member.dto.*;
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

    @Test
    void 친구요청수락() {

        // given
        // 친구요청 보낸 사람
        Long id = join();
        saveUserDetails(id);

        // 친구요청 받는 사람
        Long receiverId = ids.getFirst();
        RequestFriendshipDto requestFriendshipDto = new RequestFriendshipDto(receiverId);
        memberService.sendFriendRequest(requestFriendshipDto);
        SecurityContextHolder.clearContext();
        saveUserDetails(receiverId);


        // when
        ReceiveFriendshipDto receiveFriendshipDto = new ReceiveFriendshipDto(id);
        memberService.acceptFriendRequest(receiveFriendshipDto);

        // then
        Member requester = memberRepository.findById(id).get();
        List<Member> requesterFriends = requester.getFriends();
        Member receiver = memberRepository.findById(receiverId).get();
        List<Member> receiverFriends = receiver.getFriends();

        Assertions.assertThat(requesterFriends.contains(receiver)).isTrue();
        Assertions.assertThat(receiverFriends.contains(requester)).isTrue();

    }

    @Test
    void 친구요청거절() {

        // given
        // 친구요청 보낸 사람
        Long id = join();
        saveUserDetails(id);

        // 친구요청 받는 사람
        Long receiverId = ids.getFirst();
        RequestFriendshipDto requestFriendshipDto = new RequestFriendshipDto(receiverId);
        memberService.sendFriendRequest(requestFriendshipDto);
        SecurityContextHolder.clearContext();
        saveUserDetails(receiverId);

        // when
        memberService.rejectFriendRequest(id);
        // 💡 강제 flush + clear로 DB 동기화
        // 나중에 다시 공부
        em.flush();
        em.clear();

        // then
        Member requester = memberRepository.findById(id).get();
        Member receiver = memberRepository.findById(receiverId).get();
        // 받은 사람의 대기목록
        List<Member> receiverFriends = receiver.getPendingFriends();

        // 받은 사람의 대기목록에 없어야함
        Assertions.assertThat(receiverFriends.contains(requester)).isFalse();

    }

    @Test
    void 친구조회() {

        // given
        // 친구요청 보낸 사람
        Long id = join();
        saveUserDetails(id);

        // 친구요청 받는 사람
        Long receiverId = ids.getFirst();
        RequestFriendshipDto requestFriendshipDto = new RequestFriendshipDto(receiverId);
        memberService.sendFriendRequest(requestFriendshipDto);
        SecurityContextHolder.clearContext();
        saveUserDetails(receiverId);
        ReceiveFriendshipDto receiveFriendshipDto = new ReceiveFriendshipDto(id);
        memberService.acceptFriendRequest(receiveFriendshipDto);

        // 친구요청 보낸 사람
        Long requesterId = ids.getLast();
        RequestFriendshipDto requestFriendshipDto2 = new RequestFriendshipDto(id);
        SecurityContextHolder.clearContext();
        saveUserDetails(requesterId);
        memberService.sendFriendRequest(requestFriendshipDto2);

        SecurityContextHolder.clearContext();
        saveUserDetails(id);
        ReceiveFriendshipDto receiveFriendshipDto2 = new ReceiveFriendshipDto(requesterId);
        memberService.acceptFriendRequest(receiveFriendshipDto2);

        // when
        // id의 친구들
        List<FriendDto> friends = memberService.findAllFriends();
        SecurityContextHolder.clearContext();
        // receiver의 친구들
        saveUserDetails(receiverId);
        List<FriendDto> friends2 = memberService.findAllFriends();
        SecurityContextHolder.clearContext();
        // requester의 친구들
        saveUserDetails(requesterId);
        List<FriendDto> friends3 = memberService.findAllFriends();

        // then
        // id의 친구들중에 receiverid, requesterId가 있는지
        boolean temp1 = friends.stream().map(FriendDto::getId).toList().contains(receiverId);
        boolean temp2 = friends.stream().map(FriendDto::getId).toList().contains(requesterId);
        Assertions.assertThat(temp1).isTrue();
        Assertions.assertThat(temp2).isTrue();

        // receiverid 친구들 중에 id가 있는지
        boolean temp3 = friends2.stream().map(FriendDto::getId).toList().contains(id);
        Assertions.assertThat(temp3).isTrue();

        // requesterId 친구들 중에 id가 있는지
        boolean temp4 = friends3.stream().map(FriendDto::getId).toList().contains(id);
        Assertions.assertThat(temp4).isTrue();
    }

    @Test
    void 보류중인요청조회() {

        // given
        // 친구요청 보낸 사람
        Long id = join();
        saveUserDetails(id);

        // 친구요청 받는 사람
        Long receiverId = ids.getFirst();
        RequestFriendshipDto requestFriendshipDto = new RequestFriendshipDto(receiverId);
        memberService.sendFriendRequest(requestFriendshipDto);
        // 보낸 요청중 보류중인거
        boolean temp1 = memberService.findPendingRequest().stream().map(FriendDto::getId).toList().contains(receiverId);
        Assertions.assertThat(temp1).isTrue();
        SecurityContextHolder.clearContext();
        saveUserDetails(receiverId);

        // when

        // then


        // 받은 요청중 보류중인거
        boolean temp2 = memberService.findPendingResponse().stream().map(FriendDto::getId).toList().contains(id);
        Assertions.assertThat(temp2).isTrue();
    }

    

}