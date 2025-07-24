package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompareMemberDto {
    String year;
    String semester;
    List<Long> memberIds;

}
