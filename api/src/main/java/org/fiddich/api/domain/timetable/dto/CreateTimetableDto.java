package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateTimetableDto {

    private String year;
    private String semester;
    private String timeTableName;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<Long> selectedLectureIds;

    public boolean getIsRepresent() {
        return isRepresent;
    }
} // 클라이언트에서 시간표를 생성할때
