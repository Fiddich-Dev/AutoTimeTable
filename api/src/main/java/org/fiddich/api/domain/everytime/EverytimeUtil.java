package org.fiddich.api.domain.everytime;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.everytime.dto.Category;
import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.api.domain.timetable.dto.CreateTimetableDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class EverytimeUtil {

    // 에타의 모든 시간표 가져오기(조회만)
    public List<CreateTimetableDto> allEverytimeMapping(String url) {
        try {
            return EverytimeRequester.findAllEveryTimetable(url).stream().map(CreateTimetableDto::new).toList();
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 모든 학과 가져오기 (상위학과 제외)
    public List<Category> everytimeCategories(String year, String semester) {
        try {
            return EverytimeRequester.findAllCategories(year, semester);
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 학과번호로 전체 강의 찾기
    public List<InternalLectureDto> getLecturesByCategoryId(String categoryId, String year, String semester) {
        try {
            List<Subject> subjects = EverytimeRequester.findSubjectsByCategoryId(categoryId, year, semester);
            return subjects.stream().map(InternalLectureDto::new).toList();
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 키워드로 강의 검색하기
    public List<InternalLectureDto> searchEverytimeLectures(String type, String keyword, String year, String semester, int page, int size) {
        String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", type, keyword);
        try {
            List<Subject> subjects = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, size, page*size);
            return subjects.stream().map(InternalLectureDto::new).toList();
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

}
