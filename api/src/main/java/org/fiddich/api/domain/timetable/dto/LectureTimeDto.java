package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;

@Data
@AllArgsConstructor
public class LectureTimeDto {

    private Integer day;
    private Integer start;
    private Integer end;

    public LectureTimeDto(LectureTime lectureTime) {
        this.day = lectureTime.getDay();
        this.start = lectureTime.getStart();
        this.end = lectureTime.getEnd();
    }
}
