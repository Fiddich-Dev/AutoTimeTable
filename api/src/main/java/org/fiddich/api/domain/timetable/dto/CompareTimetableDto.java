package org.fiddich.api.domain.timetable.dto;

import lombok.Data;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompareTimetableDto {
    InternalLectureDto internalLectureDto;
    List<String> usernames = new ArrayList<>();
    List<String> studentIds = new ArrayList<>();

    public CompareTimetableDto(Lecture lecture) {
        this.internalLectureDto = new InternalLectureDto(lecture);
    }
}
