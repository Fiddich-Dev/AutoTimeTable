//package org.fiddich.api.domain.timetable;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.EntityManager;
//import jakarta.transaction.Transactional;
//import org.assertj.core.api.Assertions;
//import org.fiddich.api.domain.member.MemberService;
//import org.fiddich.api.domain.member.dto.JoinDto;
//import org.fiddich.api.domain.timetable.dto.CreateTimetableWithExternalLecturesDto;
//import org.fiddich.api.domain.timetable.dto.InquiryTimeTableDto;
//import org.fiddich.api.domain.timetable.dto.YearAndSemesterDto;
//import org.fiddich.coreinfradomain.domain.Member.Member;
//import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
//import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
//import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//@AutoConfigureMockMvc
//@TestPropertySource(properties = {
//        "JWT_SECRET=salkdhjaslkdjsalkjdaslkjasdlkahskdsahdksahkdhaskdjhsakj",
//        "SMTP_PORT=587",
//        "SMTP_USERNAME=hiws9997@gmail.com",
//        "SMTP_PASSWORD=gedu ihvz eqsg qwtn",
//})
//class EverytimeServiceTest {
//
//    @Autowired
//    EntityManager em;
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    MemberService memberService;
//    @Autowired
//    MemberRepository memberRepository;
//    @Autowired
//    EverytimeService everytimeService;
//    @Autowired
//    TimetableService timetableService;
//
//    List<Long> ids = new ArrayList<>();
//
//    // 테스트 회원들
//    @BeforeEach
//    public void init() {
//        for (int i = 1; i <= 5; i++) {
//            Member member = Member.builder()
//                    .studentId("testStudentId" + i)
//                    .password("testPassword" + i)
//                    .username("testUsername" + i)
//                    .build();
//
//            em.persist(member);
//            ids.add(member.getId());
//        }
//        em.flush();
//        em.clear();
//    }
//
//
//    @AfterEach
//    void emptySecurotyContext() {
//        // 테스트 후 SecurityContext 초기화
//        SecurityContextHolder.clearContext();
//    }
//
//    // 나 회원가입
//    public Long join() {
//        JoinDto joinDto = new JoinDto("내학번", "내비밀번호", "내이름");
//        Long id = memberService.join(joinDto);
//        return id;
//    }
//
//    public JWTDto login() throws Exception {
//        String requestBody = """
//        {
//            "studentId": "내학번",
//            "password": "내비밀번호"
//        }
//        """;
//
//        // when
//        MvcResult result = mockMvc.perform(post("/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.content.access").exists())  // content 내부에 access 토큰 확인
//                .andExpect(jsonPath("$.content.refresh").exists()) // content 내부에 refresh 토큰 확인
//                .andReturn();
//
//
//        String responseBody = result.getResponse().getContentAsString();
//        // JSON 파싱
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> map = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
//        Map<String, String> contentMap = (Map<String, String>) map.get("content");
//
//        String access = contentMap.get("access");
//        String refresh = contentMap.get("refresh");
//
//        return new JWTDto(access, refresh);
//    }
//
//    public void saveUserDetails(Long id) {
//        Member member = memberRepository.findById(id).get();
//
//        CustomUserDetails customUserDetails = new CustomUserDetails(member);
//        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        context.setAuthentication(authentication);
//        SecurityContextHolder.setContext(context);
//    }
//
//    @Test
//    @DisplayName(value = "로그확인")
//    public void 에타모든시간표조회() throws Exception {
//        // given
//        String url = "https://everytime.kr/@1eMc2T1GfAQBsE4LK7gb";
//        // when
//        List<CreateTimetableWithExternalLecturesDto> createTimetableWithExternalLecturesDtos = everytimeService.allEverytimeMapping(url);
//        createTimetableWithExternalLecturesDtos.sort(
//                Comparator.comparing(CreateTimetableWithExternalLecturesDto::getYear).reversed()
//                        .thenComparing(dto -> semesterOrder(dto.getSemester()))
//        );
//        // then
//        for(CreateTimetableWithExternalLecturesDto dto : createTimetableWithExternalLecturesDtos) {
//            System.out.println(dto.getYear() + " - " + dto.getSemester() + " , 강의개수 : " + dto.getLectures().size());
//            System.out.println(dto.getLectures().toString());
//        }
//        Assertions.assertThat(createTimetableWithExternalLecturesDtos.size()).isNotZero();
//    }
//
//    @Test
//    @DisplayName(value = "clear왜 해야하지")
////    @Rollback(value = false)
//    public void 에타시간표저장() throws Exception {
//        // given
//        String url = "https://everytime.kr/@1eMc2T1GfAQBsE4LK7gb";
//        Long myId = join();
//        saveUserDetails(myId);
//        List<CreateTimetableWithExternalLecturesDto> createTimetableWithExternalLecturesDtos = everytimeService.allEverytimeMapping(url);
//        createTimetableWithExternalLecturesDtos.sort(
//                Comparator.comparing(CreateTimetableWithExternalLecturesDto::getYear).reversed()
//                        .thenComparing(dto -> semesterOrder(dto.getSemester()))
//        );
//
//        // when
//        for(CreateTimetableWithExternalLecturesDto dto : createTimetableWithExternalLecturesDtos) {
//            everytimeService.createTimetableWithExternalLectures(dto);
//        }
//        em.flush();
//        em.clear();
//
//
//        List<YearAndSemesterDto> existYearAndSemesters = timetableService.getYearAndSemester();
//
//        // then
//        for(YearAndSemesterDto yearAndSemester : existYearAndSemesters) {
//            InquiryTimeTableDto timetable = timetableService.getTimetablesAboutYearAndSemester(yearAndSemester.getYear(), yearAndSemester.getSemester()).get(0);
//
//            int lectureCnt = createTimetableWithExternalLecturesDtos.stream()
//                    .filter(t -> t.getYear().equals(yearAndSemester.getYear()) && t.getSemester().equals(yearAndSemester.getSemester()))
//                    .toList()
//                    .get(0)
//                    .getLectures()
//                    .size();
//
//            Assertions.assertThat(timetable.getLectures().size()).isEqualTo(lectureCnt);
//        }
//    }
//
//    private int semesterOrder(String semester) {
//        return switch (semester) {
//            case "1" -> 3;
//            case "여름" -> 2;
//            case "2" -> 1;
//            case "겨울" -> 0;
//            default -> 4; // 알 수 없는 값은 맨 뒤로
//        };
//    }
//
//}