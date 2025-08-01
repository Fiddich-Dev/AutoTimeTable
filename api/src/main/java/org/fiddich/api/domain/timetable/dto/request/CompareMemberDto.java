package org.fiddich.api.domain.timetable.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompareMemberDto {
    private String year;
    private String semester;
    private List<Long> memberIds;

}
