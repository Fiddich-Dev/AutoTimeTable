package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LectureIdsDto {

    private List<Long> lectureIds;

}
