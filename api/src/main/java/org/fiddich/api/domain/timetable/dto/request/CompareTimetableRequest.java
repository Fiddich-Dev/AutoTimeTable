package org.fiddich.api.domain.timetable.dto.request;

import java.util.List;

public record CompareTimetableRequest(
    String year,
    String semester,
    List<Long> memberIds
) {
}
