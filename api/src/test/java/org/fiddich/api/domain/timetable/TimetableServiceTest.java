package org.fiddich.api.domain.timetable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.everytime.EverytimeUtil;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.member.dto.request.SignUpRequest;
import org.fiddich.api.domain.timetable.dto.*;

import org.fiddich.api.domain.timetable.dto.request.CompareTimetableRequest;
import org.fiddich.api.domain.timetable.dto.request.TimetableIdRequest;
import org.fiddich.api.domain.timetable.dto.response.CompareTimetableDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.Timetable.repository.TimetableRepository;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
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

import java.util.*;

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
class TimetableServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EverytimeUtil everytimeUtil;
    @Autowired
    TimetableService timetableService;
    @Autowired
    private TimetableRepository timetableRepository;

    List<Long> ids = new ArrayList<>();

    List<String> officialLectureCodeSections = List.of("GEDB003-01", "GEDB003-41", "GEDB003-42");

    List<InternalLectureDto> encodedCustomLectures = List.of(
            new InternalLectureDto(1L, "", "커스텀강의1", "교수1", "", "0", "", "일900-1100,일1200-1300"),
            new InternalLectureDto(2L, "", "커스텀강의2", "교수2", "", "0", "", "일1330-1445,일1500-1600"),
            new InternalLectureDto(3L, "", "커스텀강의3", "교수3", "", "0", "", "일1600-1800")
    );

    List<String> officialLectureCodeSections2 = List.of("GEDB003-42", "GEDB003-43", "GEDB003-44");

    List<InternalLectureDto> encodedCustomLectures2 = List.of(
            new InternalLectureDto(1L, "", "커스텀강의3", "교수3", "", "0", "", "토900-1100,토1200-1300"),
            new InternalLectureDto(2L, "", "커스텀강의4", "교수4", "", "0", "", "토1330-1445,토1500-1600"),
            new InternalLectureDto(3L, "", "커스텀강의5", "교수5", "", "0", "", "토1600-1800")
    );



    // 테스트 회원들
    @BeforeEach
    public void init() {
        // ID 시퀀스 초기화
//        em.createNativeQuery("ALTER TABLE lecture ALTER COLUMN lecture_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN member_id RESTART WITH 1").executeUpdate();
//        em.createNativeQuery("ALTER TABLE timetable ALTER COLUMN timetable_id RESTART WITH 1").executeUpdate();


        // 멤버 생성
        ids.clear();
        for (int i = 1; i <= 5; i++) {
            Member member = Member.builder()
                    .studentId("testStudentId" + i)
                    .password("testPassword" + i)
                    .username("testUsername" + i)
                    .build();
            em.persist(member);
            ids.add(member.getId());
        }
    }


    @AfterEach
    void emptySecurotyContext() {
        // 테스트 후 SecurityContext 초기화
        SecurityContextHolder.clearContext();
        em.clear();
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

    String nowYear = "2025";
    String nowSemester = "2";


    @Test
//    @DisplayName("claer 왜 해야하는지")
//    @Rollback(value = false)
    public void 시간표저장() {
        // given
        Long myId = join();
        saveUserDetails(myId);

        List<InternalLectureDto> saveLectures = new ArrayList<>();
        saveLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }

        CreateTimetableDto createTimetableDto = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표1", false, saveLectures);

        // when
        Long timetableId = timetableService.save(createTimetableDto);
        em.flush();
        em.clear();

        // then
        InquiryTimeTableDto findTimetable = timetableService.getTimetablesAboutYearAndSemester(nowYear, nowSemester).get(0);
        List<InternalLectureDto> findLectures = findTimetable.getLectures();
        Assertions.assertThat(findTimetable.getYear()).isEqualTo(nowYear);
        Assertions.assertThat(findTimetable.getSemester()).isEqualTo(nowSemester);
        Assertions.assertThat(findLectures).containsAll(encodedCustomLectures);
        Assertions.assertThat(findLectures.stream().map(l -> l.getCodeSection())).containsAll(officialLectureCodeSections);
        Assertions.assertThat(timetableService.getMainTimetableWithLectures(nowYear, nowSemester)).isNull();
    }

    @Test
//    @DisplayName("clear 왜 해야하는지")
    public void 메인시간표조회() {
        // given
        Long myId = join();
        saveUserDetails(myId);

        List<InternalLectureDto> saveLectures = new ArrayList<>();
        saveLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }

        CreateTimetableDto createTimetableDto = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표1", true, saveLectures);

        // when
        Long timetableId = timetableService.save(createTimetableDto);
        em.flush();
        em.clear();

        // then
        InquiryTimeTableDto findTimetable = timetableService.getMainTimetableWithLectures(nowYear, nowSemester);
        List<InternalLectureDto> findLectures = findTimetable.getLectures();
        Assertions.assertThat(findTimetable.getYear()).isEqualTo(nowYear);
        Assertions.assertThat(findTimetable.getSemester()).isEqualTo(nowSemester);
        Assertions.assertThat(findLectures).containsAll(encodedCustomLectures);
        Assertions.assertThat(findLectures.stream().map(l -> l.getCodeSection())).containsAll(officialLectureCodeSections);
    }

    @Test
//    @DisplayName("clear 왜 해야하는지")
//    @Rollback(value = false)
    public void 시간표수정() {
        // given
        Long myId = join();
        saveUserDetails(myId);

        List<InternalLectureDto> saveLectures = new ArrayList<>();
        saveLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }

        CreateTimetableDto createTimetableDto = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표1", false, saveLectures);
        Long timetableId = timetableService.save(createTimetableDto);

        // when
        List<InternalLectureDto> saveLectures2 = new ArrayList<>();
        saveLectures2.addAll(encodedCustomLectures2);
        for(String codeSection : officialLectureCodeSections2) {
            saveLectures2.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        timetableService.editTimetable(timetableId, saveLectures2);
        em.flush();
        em.clear();

        // then
        InquiryTimeTableDto findTimetable = timetableService.getTimetablesAboutYearAndSemester(nowYear, nowSemester).get(0);
        List<InternalLectureDto> findLectures = findTimetable.getLectures();
        Assertions.assertThat(findTimetable.getYear()).isEqualTo(nowYear);
        Assertions.assertThat(findTimetable.getSemester()).isEqualTo(nowSemester);
        Assertions.assertThat(findLectures).containsAll(encodedCustomLectures2);
        Assertions.assertThat(findLectures.stream().map(l -> l.getCodeSection())).containsAll(officialLectureCodeSections2);
    }

    @Test
    public void 시간표삭제() {
        // given
        Long myId = join();
        saveUserDetails(myId);

        List<InternalLectureDto> saveLectures = new ArrayList<>();
        saveLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }

        CreateTimetableDto createTimetableDto = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표1", false, saveLectures);
        Long timetableId = timetableService.save(createTimetableDto);

        // when
        timetableService.deleteTimetable(timetableId);

        // then
        List<InquiryTimeTableDto> findTimetable = timetableService.getTimetablesAboutYearAndSemester(nowYear, nowSemester);
        Assertions.assertThat(findTimetable.size()).isZero();
    }

    @Test
//    @DisplayName("clear 왜 해야하는지")
    public void 메인시간표변경() {
        // given
        Long myId = join();
        saveUserDetails(myId);

        // 1번쨰 시간표
        List<InternalLectureDto> saveLectures = new ArrayList<>();
        saveLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        CreateTimetableDto createTimetableDto = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표1", true, saveLectures);
        Long timetableId = timetableService.save(createTimetableDto);

        List<InternalLectureDto> saveLectures2 = new ArrayList<>();
        saveLectures2.addAll(encodedCustomLectures2);
        for(String codeSection : officialLectureCodeSections2) {
            saveLectures2.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        CreateTimetableDto createTimetableDto2 = new CreateTimetableDto(nowYear, nowSemester, "테스트시간표2", false, saveLectures2);
        Long timetableId2 = timetableService.save(createTimetableDto2);

        // when
        timetableService.changeMainTimetable(new TimetableIdRequest(timetableId2));
        em.flush();
        em.clear();

        // then
        InquiryTimeTableDto timetable = timetableService.getMainTimetableWithLectures(nowYear, nowSemester);
        Assertions.assertThat(timetable.getLectures()).containsAll(encodedCustomLectures2);
        Assertions.assertThat(timetable.getLectures().stream().map(l -> l.getCodeSection())).containsAll(officialLectureCodeSections2);
    }

    @Test
    public void 겹치는강의조회() {
        // given
        Long friendId = 1L;
        saveUserDetails(friendId);
        List<InternalLectureDto> saveFriendLectures = new ArrayList<>();
        saveFriendLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveFriendLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        Long friendTimetableId = timetableService.save(new CreateTimetableDto(nowYear, nowSemester, "테스트1", true, saveFriendLectures));
        SecurityContextHolder.clearContext();

        List<InternalLectureDto> mySaveLectures = new ArrayList<>();
        mySaveLectures.addAll(encodedCustomLectures2);
        for(String codeSection : officialLectureCodeSections2) {
            mySaveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        Long myId = join();
        saveUserDetails(myId);
        Long myTimetableId = timetableService.save(new CreateTimetableDto(nowYear, nowSemester, "테스트2", true, mySaveLectures));

        // when
        CompareTimetableRequest compareTimetableRequest = new CompareTimetableRequest(nowYear, nowSemester, List.of(friendId));
        List<CompareTimetableDto> compareTimetableDtos = timetableService.compareTimetable(compareTimetableRequest);
        em.flush();
        em.clear();

        // then
        // 공식 강의만 겹쳐야함
        Member friend = memberRepository.findById(friendId).get();

        // 교집합 구하기 (공식 강의 기준)
        Set<String> expectedCodeSections = new HashSet<>(officialLectureCodeSections);
        expectedCodeSections.retainAll(officialLectureCodeSections2);

        // 실제 비교 결과의 codeSection 추출
        List<String> actualCodeSections = compareTimetableDtos.stream()
                .map(dto -> dto.getInternalLectureDto().getCodeSection())
                .toList();

        // 공식 강의 교집합이 정확히 포함되어 있는지 (순서 무시)
        Assertions.assertThat(actualCodeSections)
                .containsExactlyInAnyOrderElementsOf(expectedCodeSections);

        // 친구 정보 검증
        for (CompareTimetableDto dto : compareTimetableDtos) {
            Assertions.assertThat(dto.getUsernames()).containsExactly(friend.getUsername());
            Assertions.assertThat(dto.getStudentIds()).containsExactly(friend.getStudentId());
        }
    }

    @Test
    public void 겹치는시간조회() {
        // given
        Long friendId = 1L;
        saveUserDetails(friendId);
        List<InternalLectureDto> saveFriendLectures = new ArrayList<>();
        saveFriendLectures.addAll(encodedCustomLectures);
        for(String codeSection : officialLectureCodeSections) {
            saveFriendLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        Long friendTimetableId = timetableService.save(new CreateTimetableDto(nowYear, nowSemester, "테스트1", true, saveFriendLectures));
        SecurityContextHolder.clearContext();

        List<InternalLectureDto> mySaveLectures = new ArrayList<>();
        mySaveLectures.addAll(encodedCustomLectures2);
        for(String codeSection : officialLectureCodeSections2) {
            mySaveLectures.add(everytimeUtil.searchEverytimeLectures("code", codeSection, nowYear, nowSemester, 0, 50).get(0));
        }
        Long myId = join();
        saveUserDetails(myId);
        Long myTimetableId = timetableService.save(new CreateTimetableDto(nowYear, nowSemester, "테스트2", true, mySaveLectures));
        em.flush();
        em.clear();

        // when
        List<Long> friendIds = new ArrayList<>();
        friendIds.add(friendId);
        CompareTimetableRequest compareTimetableRequest = new CompareTimetableRequest(nowYear, nowSemester, friendIds);
        List<InternalLectureDto> allLectures = timetableService.compareFreeTime(compareTimetableRequest);
        em.flush();
        em.clear();

        System.out.println("찾은거");

        // then
        List<InternalLectureDto> allSaveLectures = new ArrayList<>();
        allSaveLectures.addAll(mySaveLectures);
        allSaveLectures.addAll(saveFriendLectures);

        Assertions.assertThat(allLectures)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringCollectionOrder()
                .isEqualTo(allSaveLectures);
    }


}