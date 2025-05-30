package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class TimeTableDto2 {

    private Long id;
    private String year;
    private String semester;
    private String timeTableName;
    private boolean isRepresent;
    private List<TimetableLectureDto> lectures;
}
