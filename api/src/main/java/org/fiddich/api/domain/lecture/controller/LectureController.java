package org.fiddich.api.domain.lecture.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.lecture.LectureService;
import org.fiddich.api.domain.lecture.dto.InquiryDepartmentDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping("/lectures/search")
    public ApiResponse<List<InternalLectureDto>> searchLectures(@RequestParam String keyword) {
        log.info("{}로 강의 검색", keyword);
        return ApiResponse.onSuccess(lectureService.searchLecturesByKeyword(keyword));
    }

    @GetMapping("/categories")
    public ApiResponse<List<InquiryDepartmentDto>> getAllCategories(@RequestParam String year, @RequestParam String semester) {
        log.info("모든 학과 조회 year = {}, semester = {}", year, semester);
        return ApiResponse.onSuccess(lectureService.getAllCategories(year, semester));
    }
}
