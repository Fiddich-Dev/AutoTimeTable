package org.fiddich.api.domain.everytime;


import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.fiddich.api.domain.everytime.dto.Category;
import org.fiddich.api.domain.timetable.dto.CreateTimetableDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.List;


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
public class EverytimeUtilTest {

    @Autowired
    EverytimeUtil everytimeUtil;

    String nowYear = "2025";
    String nowSemester = "2";

    @Test
    public void 에타모든시간표조회() {
        String url = "https://everytime.kr/@1eMc2T1GfAQBsE4LK7gb";
        List<CreateTimetableDto> createTimetableDtos = everytimeUtil.allEverytimeMapping(url);
        Assertions.assertThat(createTimetableDtos.size()).isNotZero();
    }

    @Test
    @DisplayName(value = "에타 모든학과 조회 (상위학과 제외)")
    public void 에타모든학과조회() {
        List<Category> categories = everytimeUtil.everytimeCategories(nowYear, nowSemester);
        Assertions.assertThat(categories.size()).isNotZero();
    }

    @Test
    @DisplayName(value = "에타 학과id로 강의 전체 조회 (상위학과 제외, 페이징X)")
    public void 에타학과강의조회() {
        List<Category> categories = everytimeUtil.everytimeCategories(nowYear, nowSemester);
        String categoryId = categories.get(0).getId();
        List<InternalLectureDto> lectureDtos = everytimeUtil.getLecturesByCategoryId(categoryId, nowYear, nowSemester);
        Assertions.assertThat(lectureDtos.size()).isNotZero();
    }

    @Test
    public void 키워드로에타강의검색() {
        String type = "code";
        String keyword = "GE";
        List<InternalLectureDto> lectureDtos = everytimeUtil.searchEverytimeLectures(type, keyword, nowYear, nowSemester, 0, 50);
        Assertions.assertThat(lectureDtos.size()).isNotZero();
    }

}
