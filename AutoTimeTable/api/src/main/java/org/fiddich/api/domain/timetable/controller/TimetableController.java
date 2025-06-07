package org.fiddich.api.domain.timetable.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.dto.LectureDto;
import org.fiddich.api.domain.timetable.dto.TimeTableDto2;
import org.fiddich.api.domain.timetable.dto.TimetableDto;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.api.domain.timetable.dto.YearAndSemesterDto;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.jsoup.Connection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.sql.Time;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping("/timetable/save")
    public ApiResponse<Long> saveTimetable(@RequestBody TimetableDto timetableDto) {
        log.info("save");
        return ApiResponse.onSuccess(timetableService.save(timetableDto));
    }

    @GetMapping("/timetable/yearAndSemester")
    public ApiResponse<List<YearAndSemesterDto>> getYearAndSemester() {
        log.info("getYearAndSemester");
        return ApiResponse.onSuccess(timetableService.getYearAndSemester());
    }

    @GetMapping("/timetables")
    public ApiResponse<List<TimeTableDto2>> getTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        log.info("getTimetablesWithLectures");
        return ApiResponse.onSuccess(timetableService.getTimetablesWithLectures(year, semester));
    }

    @GetMapping("/timetable/everytime")
    public String getTimeTableXml(@RequestParam String url) throws Exception {
        log.info("getTimeTableXml");
        String[] parts = url.split("/");
        String identifier = parts[parts.length - 1].replace("@", "");


        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/table/friend")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://everytime.kr/")
                .data("identifier", identifier)
                .data("friendInfo", "true")
                .timeout(5000)
                .post(); // ← 여기 수정

        return doc.outerHtml();
    }

    @PutMapping("/timetable/edit/{timetableId}")
    public ApiResponse<Void> editTimetable(@PathVariable Long timetableId, @RequestBody LectureDto lectureDto) {
        log.info("editTimetable");
        timetableService.editTimetable(timetableId, lectureDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/timetable/delete/{timetableId}")
    public ApiResponse<Void> deleteTimetable(@PathVariable Long timetableId) {
        log.info("deleteTimetable");
        timetableService.deleteTimetable(timetableId);
        return ApiResponse.onSuccess(null);
    }





    //        for (Timetable timetable : timetables) {
//            System.out.println("📅 시간표: " + timetable.getYear() + "년 " + timetable.getSemester() + "학기");
//
//            for (TimetableLecture lecture : timetable.getTimetableLectures()) {
//                System.out.println("   📖 강의: " + lecture.getLectureName());
//            }
//        }
}
