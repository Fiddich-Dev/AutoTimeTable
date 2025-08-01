package org.fiddich.api.domain.timetable.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateTimetableOptionDto {

    String year;
    String semester;

    int targetMajorCnt;
    int targetCultureCnt;
    List<String> likeOfficialLectureCodeSection;
    List<String> dislikeOfficialLectureCodeSection;
    List<String> categoryIds;
    int[][] usedTime;

    int minCredit;
    int maxCredit;

    private boolean preferMorning;
    private boolean preferAfternoon;

}
