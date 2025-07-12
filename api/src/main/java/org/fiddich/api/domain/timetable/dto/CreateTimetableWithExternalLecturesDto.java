package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CreateTimetableWithExternalLecturesDto {

    private String year;
    private String semester;
    private String timeTableName;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<ExternalLectureDto> lectures;

    public boolean isRepresent() {
        return isRepresent;
    }
} // 외부 DB에서 시간표를 조회할떄 사용
