package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateTimetableOptionDto {

    int targetMajorCnt;
    int targetCultureCnt;
    List<Long> likeLectureCode;
    List<Long> dislikeLectureCode;
    List<Long> categoryIds;
    int[][] usedTime;

    int minCredit;
    int maxCredit;

    private boolean preferMorning;
    private boolean preferAfternoon;

}
