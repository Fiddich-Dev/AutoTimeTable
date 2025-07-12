package org.fiddich.api.domain.timetable.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompareMemberDto {
    String year;
    String semester;
    List<Long> memberIds;

}
