package com.fiddich.LectureMapping.nouse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureDataInitializer {
    private final CategoryService categoryService;
    private final LectureService lectureService;
    private final SchoolService schoolService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() throws Exception {
        String year = "2025";
        String semester = "1";

        // 학교 정보 설정
        schoolService.initializeSchool();

        // 카테고리 정보 수집 및 저장
        categoryService.fetchAndSaveCategories(year, semester);

        // 강의 정보 수집 및 저장
        lectureService.fetchAndSaveAllLectures(year, semester, 50);
    }
}
