package org.fiddich.api.domain.timetable.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TimetableController {
    // 시간표에 커스텀 시간표 안뜬다, 저장은 됨
    private final TimetableService timetableService;

    @PostMapping("/timetables")
    public ApiResponse<Long> saveTimetable(@RequestBody CreateTimetableDto createTimetableDto) {
        log.info("시간표 저장: {}", createTimetableDto);
        return ApiResponse.onSuccess(timetableService.save(createTimetableDto));
    }

    @GetMapping("/timetables")
    public ApiResponse<List<InquiryTimeTableDto>> getTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        log.info("학년도의 시간표 조회 year = {}, semester = {}", year, semester);
        return ApiResponse.onSuccess(timetableService.getTimetablesAboutYearAndSemester(year, semester));
    }

    @GetMapping("/timetables/periods")
    public ApiResponse<List<YearAndSemesterDto>> getYearAndSemester() {
        log.info("존재하는 학년도 조회");
        return ApiResponse.onSuccess(timetableService.getYearAndSemester());
    }

    @GetMapping("/timetables/main")
    public ApiResponse<InquiryTimeTableDto> getMainTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        log.info("메인시간표 조회 year = {}, semester = {}", year, semester);
        InquiryTimeTableDto mainTimetable = timetableService.getMainTimetableWithLectures(year, semester);
        if(mainTimetable != null) {
            return ApiResponse.onSuccess(mainTimetable);
        }
        else {
            return ApiResponse.onFailure("error", "No main timetable exists.");
        }
    }

    @PostMapping("/timetables/everytime")
    public ApiResponse<Void> saveEveryTimetable(@RequestBody CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto) {
        log.info("에타 시간표 저장: {]", createTimetableWithExternalLecturesDto);
        timetableService.createTimetableWithExternalLectures(createTimetableWithExternalLecturesDto);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/timetables/everytime")
    public ApiResponse<List<CreateTimetableWithExternalLecturesDto>> getAlleverytime(@RequestParam String url) throws Exception {
        log.info("에타 시간표 모두 가져오기: {}", url);
        List<CreateTimetableWithExternalLecturesDto> timetables = timetableService.allEverytimeMapping(url);
        return ApiResponse.onSuccess(timetables);
    }

    @PutMapping("/timetables/{timetableId}")
    public ApiResponse<Void> editTimetable(@PathVariable Long timetableId, @RequestBody LectureIdsDto lectureIdsDto) {
        log.info("{}번 시간표 대체: {}", timetableId, lectureIdsDto);
        timetableService.editTimetable(timetableId, lectureIdsDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/timetables/{timetableId}")
    public ApiResponse<Void> deleteTimetable(@PathVariable Long timetableId) {
        log.info("{}번 시간표 삭제", timetableId);
        timetableService.deleteTimetable(timetableId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/timetables/auto-generate")
    public ApiResponse<List<List<InternalLectureDto>>> createTimetable(@RequestBody CreateTimetableOptionDto optionDto) {
        log.info("시간표 생성, 옵션: {]", optionDto.toString());
        List<List<InternalLectureDto>> fullList = timetableService.createTimetable(optionDto);
        List<List<InternalLectureDto>> subList = fullList.subList(0, Math.min(50, fullList.size()));
        return ApiResponse.onSuccess(subList);
    }

    @PatchMapping("/timetables/main")
    public ApiResponse<Void> changeMainTimetable(@RequestBody TimetableIdDto timetableIdDto) {
        log.info("{}번으로 메인 시간표 변경", timetableIdDto);
        timetableService.changeMainTimetable(timetableIdDto);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/lectures/search")
    public ApiResponse<List<InternalLectureDto>> searchLectures(@RequestParam String keyword) {
        log.info("{}로 강의 검색", keyword);
        return ApiResponse.onSuccess(timetableService.searchLecturesByKeyword(keyword));
    }
    // 아마 안쓸듯
    @GetMapping("/lectures")
    public ApiResponse<List<Lecture>> getAllLectures() {
        log.info("모든 강의 조회");
        return ApiResponse.onSuccess(timetableService.getAllLectures());
    }

    @GetMapping("/categories")
    public ApiResponse<List<InquiryDepartmentDto>> getAllCategories(@RequestParam String year, @RequestParam String semester) {
        log.info("모든 학과 조회 year = {}, semester = {}", year, semester);
        return ApiResponse.onSuccess(timetableService.getAllCategories(year, semester));
    }

    @PostMapping("/timetables/compare-lecture")
    public ApiResponse<List<CompareTimetableDto>> compareTimetable(@RequestBody CompareMemberDto compareMemberDto) {
        log.info("겹치는 강의 비교");
        return ApiResponse.onSuccess(timetableService.compareTimetable(compareMemberDto));
    }


    @PostMapping("/timetables/compare-time")
    public ApiResponse<List<InternalLectureDto>> compareFreeTime(@RequestBody CompareMemberDto compareMemberDto) {
        log.info("겹치는 시간 비교");
        return ApiResponse.onSuccess(timetableService.compareFreeTime(compareMemberDto));
    }

}
