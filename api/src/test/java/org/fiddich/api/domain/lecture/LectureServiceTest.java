package org.fiddich.api.domain.lecture;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.friend.FriendService;
import org.fiddich.api.domain.lecture.dto.InquiryDepartmentDto;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.member.dto.JoinDto;
import org.fiddich.api.domain.timetable.EverytimeService;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.fiddich.coreinfradomain.domain.Lecture.Category;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.Timetable.repository.TimetableRepository;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        "JWT_SECRET=salkdhjaslkdjsalkjdaslkjasdlkahskdsahdksahkdhaskdjhsakj",
        "SMTP_PORT=587",
        "SMTP_USERNAME=hiws9997@gmail.com",
        "SMTP_PASSWORD=gedu ihvz eqsg qwtn",
})
public class LectureServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EverytimeService everytimeService;
    @Autowired
    TimetableService timetableService;
    @Autowired
    LectureService lectureService;

    List<Long> ids = new ArrayList<>();
    @Autowired
    private TimetableRepository timetableRepository;

    // 테스트 회원들
    @BeforeEach
    public void init() {
        // ID 시퀀스 초기화
        em.createNativeQuery("ALTER TABLE lecture ALTER COLUMN lecture_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN member_id RESTART WITH 1").executeUpdate();

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

    String nowYear = "2025";
    String nowSemester = "1";


    @Test
    public void 강의검색() {
        // given
        Long myId = join();
        Member me = memberRepository.findById(myId).get();

        Lecture lecture = Lecture.builder()
                .member(me)
                .build();

        em.persist(lecture);

        // when
        List<InternalLectureDto> searchedLectures1 = lectureService.searchLecturesByKeyword("교수");
        List<InternalLectureDto> searchedLectures2 = lectureService.searchLecturesByKeyword("강의");
        List<InternalLectureDto> searchedLectures3 = lectureService.searchLecturesByKeyword("");
        List<Lecture> allLectures = em.createQuery("select l from Lecture l", Lecture.class)
                .getResultList();

        // then
        Assertions.assertThat(searchedLectures1.size()).isEqualTo(20);
        Assertions.assertThat(searchedLectures2.size()).isEqualTo(20);
        Assertions.assertThat(searchedLectures3.size()).isEqualTo(0);
        Assertions.assertThat(allLectures.size()).isEqualTo(21);
    }

    @Test
    public void 모든학과조회() {
        Category category1 = new Category();
        category1.setYear(nowYear);
        category1.setSemester(nowSemester);
        category1.setName("학과1");
        Category category1_1 = new Category();
        category1_1.setYear(nowYear);
        category1_1.setSemester(nowSemester);
        category1_1.setName("학과1-1");
        Category category2_1 = new Category();
        category2_1.setYear(nowYear);
        category2_1.setSemester(nowSemester);
        category2_1.setName("학과2-1");
        category1_1.setParent(category1);
        category2_1.setParent(category1);

        em.persist(category1);
        em.persist(category1_1);
        em.persist(category2_1);

        Category category3 = new Category();
        category3.setYear("otherYear");
        category3.setSemester("otherSemester");
        category3.setName("다른학기학과");
        category3.setParent(category1);
        em.persist(category3);

        List<InquiryDepartmentDto> allCategories = lectureService.getAllCategories(nowYear, nowSemester);

        for(InquiryDepartmentDto departmentDto : allCategories) {
            System.out.println(departmentDto.getName());
        }

        Assertions.assertThat(allCategories.size()).isEqualTo(2);
    }

}
