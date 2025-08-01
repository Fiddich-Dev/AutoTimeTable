package org.fiddich.api.domain.timetable.dto.response;

import lombok.Data;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompareTimetableDto {
    InternalLectureDto internalLectureDto;
    List<String> usernames = new ArrayList<>();
    List<String> studentIds = new ArrayList<>();

}
