package org.fiddich.api.domain.timetable.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.jsoup.Connection;
import org.springframework.web.bind.annotation.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    // 강의id가 없으면 강의를 저장하고 시간표 저장
    @PostMapping("/timetable/save")
    public ApiResponse<Long> saveTimetable(@RequestBody CreateTimetableDto createTimetableDto) {
        log.info("save");
        return ApiResponse.onSuccess(timetableService.save(createTimetableDto));
    }

    @GetMapping("/timetable/yearAndSemester")
    public ApiResponse<List<YearAndSemesterDto>> getYearAndSemester() {
        log.info("getYearAndSemester");
        return ApiResponse.onSuccess(timetableService.getYearAndSemester());
    }

    @GetMapping("/timetables")
    public ApiResponse<List<InquiryTimeTableDto>> getTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        log.info("getTimetablesWithLectures");
        return ApiResponse.onSuccess(timetableService.getTimetablesAboutYearAndSemester(year, semester));
    }

    @GetMapping("/getMainTimetable")
    public ApiResponse<InquiryTimeTableDto> getMainTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        log.info("getMainTimetableWithLectures");
        InquiryTimeTableDto mainTimetable = timetableService.getMainTimetableWithLectures(year, semester);
        if(mainTimetable != null) {
            return ApiResponse.onSuccess(mainTimetable);
        }
        else {
            return ApiResponse.onFailure("error", "No main timetable exists.");
        }
    }

    @PostMapping("/timetable/saveEveryTimetable")
    public ApiResponse<Void> saveEveryTimetable(@RequestBody CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto) {
        log.info("saveEveryTimetable");
        timetableService.createTimetableWithExternalLectures(createTimetableWithExternalLecturesDto);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/timetable/getAlleverytime")
    public ApiResponse<List<CreateTimetableWithExternalLecturesDto>> getAlleverytime(@RequestParam String url) throws Exception {
        log.info("getAlleverytime");
        List<CreateTimetableWithExternalLecturesDto> timetables = timetableService.allEverytimeMapping(url);
        return ApiResponse.onSuccess(timetables);
    }

    @PutMapping("/timetable/edit/{timetableId}")
    public ApiResponse<Void> editTimetable(@PathVariable Long timetableId, @RequestBody LectureIdsDto lectureIdsDto) {
        log.info("editTimetable");
        timetableService.editTimetable(timetableId, lectureIdsDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/timetable/delete/{timetableId}")
    public ApiResponse<Void> deleteTimetable(@PathVariable Long timetableId) {
        log.info("deleteTimetable");
        timetableService.deleteTimetable(timetableId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/getAllLectures")
    public ApiResponse<List<Lecture>> getAllLectures() {
        log.info("getAllLectures");
        return ApiResponse.onSuccess(timetableService.getAllLectures());
    }

    @PostMapping("/timetable/create")
    public ApiResponse<List<List<Lecture>>> createTimetable(@RequestBody CreateTimetableOptionDto optionDto) {
        log.info("createTimetable");
        List<List<Lecture>> fullList = timetableService.createTimetable(optionDto);
        List<List<Lecture>> subList = fullList.subList(0, Math.min(10, fullList.size()));
        return ApiResponse.onSuccess(subList);
    }

    @PatchMapping("/timetable/changeMainTimetable")
    public ApiResponse<Void> changeMainTimetable(@RequestBody TimetableIdDto timetableIdDto) {
        log.info("changeMainTimetable");
        timetableService.changeMainTimetable(timetableIdDto);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/lectures/search")
    public ApiResponse<List<InternalLectureDto>> searchLectures(@RequestParam String keyword) {
        return ApiResponse.onSuccess(timetableService.searchLecturesByKeyword(keyword));
    }

}
