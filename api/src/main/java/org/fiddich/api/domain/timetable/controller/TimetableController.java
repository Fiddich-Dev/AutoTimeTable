package org.fiddich.api.domain.timetable.controller;

import lombok.RequiredArgsConstructor;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.api.domain.timetable.dto.request.CompareTimetableRequest;
import org.fiddich.api.domain.timetable.dto.request.CreateTimetableOptionDto;
import org.fiddich.api.domain.timetable.dto.request.TimetableIdRequest;
import org.fiddich.api.domain.timetable.dto.response.CompareTimetableDto;
import org.fiddich.api.domain.timetable.dto.response.YearAndSemesterResponse;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping("/timetables")
    public ApiResponse<Long> saveTimetable(@RequestBody CreateTimetableDto createTimetableDto) {
        return ApiResponse.onSuccess(timetableService.save(createTimetableDto));
    }

    @GetMapping("/timetables")
    public ApiResponse<List<InquiryTimeTableDto>> getTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        return ApiResponse.onSuccess(timetableService.getTimetablesAboutYearAndSemester(year, semester));
    }

    @GetMapping("/timetables/periods")
    public ApiResponse<List<YearAndSemesterResponse>> getYearAndSemester() {
        return ApiResponse.onSuccess(timetableService.getYearAndSemester());
    }

    @GetMapping("/timetables/main")
    public ApiResponse<InquiryTimeTableDto> getMainTimetablesWithLectures(@RequestParam String year, @RequestParam String semester) {
        InquiryTimeTableDto mainTimetable = timetableService.getMainTimetableWithLectures(year, semester);
        if(mainTimetable != null) {
            return ApiResponse.onSuccess(mainTimetable);
        }
        else {
            return ApiResponse.onFailure("error", "No main timetable exists.");
        }
    }

    @PutMapping("/timetables/{timetableId}")
    public ApiResponse<Void> editTimetable(@PathVariable Long timetableId, @RequestBody List<InternalLectureDto> internalLectureDtos) {
        timetableService.editTimetable(timetableId, internalLectureDtos);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/timetables/{timetableId}")
    public ApiResponse<Void> deleteTimetable(@PathVariable Long timetableId) {
        timetableService.deleteTimetable(timetableId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/timetables/auto-generate")
    public ApiResponse<List<List<InternalLectureDto>>> createTimetable(@RequestBody CreateTimetableOptionDto optionDto) {
        List<List<InternalLectureDto>> fullList = timetableService.createTimetable(optionDto);
        List<List<InternalLectureDto>> subList = fullList.subList(0, Math.min(50, fullList.size()));
        return ApiResponse.onSuccess(subList);
    }

    @PatchMapping("/timetables/main")
    public ApiResponse<Void> changeMainTimetable(@RequestBody TimetableIdRequest timetableIdRequest) {
        timetableService.changeMainTimetable(timetableIdRequest);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/timetables/compare-lecture")
    public ApiResponse<List<CompareTimetableDto>> compareTimetable(@RequestBody CompareTimetableRequest compareTimetableRequest) {
        return ApiResponse.onSuccess(timetableService.compareTimetable(compareTimetableRequest));
    }

    @PostMapping("/timetables/compare-time")
    public ApiResponse<List<InternalLectureDto>> compareFreeTime(@RequestBody CompareTimetableRequest compareTimetableRequest) {
        return ApiResponse.onSuccess(timetableService.compareFreeTime(compareTimetableRequest));
    }
}
