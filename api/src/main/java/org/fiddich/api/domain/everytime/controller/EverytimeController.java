package org.fiddich.api.domain.everytime.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.everytime.EverytimeUtil;
import org.fiddich.api.domain.everytime.dto.Category;
import org.fiddich.api.domain.timetable.dto.CreateTimetableDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EverytimeController {

    private final EverytimeUtil everytimeUtil;

    // 검색해서 강의 가져오기
    @GetMapping("/everytime/lectures/search")
    public ApiResponse<List<InternalLectureDto>> searchEverytimeLectures(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam String year,
            @RequestParam String semester,
            @RequestParam int page,
            @RequestParam int size
    ) {
        log.info("에브리타임에서 강의 검색");
        return ApiResponse.onSuccess(everytimeUtil.searchEverytimeLectures(type, keyword, year, semester, page, size));
    }

    @GetMapping("/everytime/categories")
    public ApiResponse<List<Category>> getAllCategories(@RequestParam String year, @RequestParam String semester) {
        log.info("모든 학과 조회 year = {}, semester = {}", year, semester);
        return ApiResponse.onSuccess(everytimeUtil.everytimeCategories(year, semester));
    }

    @GetMapping("/everytime/timetables")
    public ApiResponse<List<CreateTimetableDto>> getAllEverytimeTables(@RequestParam String url) {
        log.info("에타 시간표 모두 가져오기: {}", url);
        List<CreateTimetableDto> timetables = everytimeUtil.allEverytimeMapping(url);
        return ApiResponse.onSuccess(timetables);
    }

}
