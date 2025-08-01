package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fiddich.api.domain.everytime.dto.TimetableByUrl;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimetableDto {

    private String year;
    private String semester;
    private String timeTableName;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<InternalLectureDto> lectures;

//    public boolean isRepresent() {
//        return isRepresent;
//    }

    public CreateTimetableDto(TimetableByUrl timetableByUrl) {
        this.year = timetableByUrl.getYear();
        this.semester = timetableByUrl.getSemester();
        this.timeTableName = timetableByUrl.getTimeTableName();
        this.isRepresent = timetableByUrl.isRepresent();
        this.lectures = timetableByUrl.getSubjects().stream().map(InternalLectureDto::new).toList();
    }


} // 외부 DB에서 시간표를 조회할떄 사용
