package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.sql.Time;
import java.util.List;


@Getter
@AllArgsConstructor
public class InquiryTimeTableDto {

    private Long id;
    private String year;
    private String semester;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<InternalLectureDto> lectures;

    public boolean isRepresent() {
        return isRepresent;
    }


    public InquiryTimeTableDto(Timetable timetable) {
        this.id = timetable.getId();
        this.year = timetable.getYear();
        this.semester = timetable.getSemester();
        this.isRepresent = timetable.getIsRepresent();
        this.lectures = timetable.getTimetableLectures().stream()
                .map(tl -> new InternalLectureDto(tl.getLecture()))
                .toList();
    }

} // 조회기능만 한다, 하지만 id로 클라이언트에서 수정요청을 날릴 수 있다
