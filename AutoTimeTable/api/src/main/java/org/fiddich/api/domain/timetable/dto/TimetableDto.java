package org.fiddich.api.domain.timetable.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TimetableDto {

    private String year;
    private String semester;
    private String timeTableName;
    private Boolean isRepresent;
    private List<Long> selectedLectureIds;

}
