package org.fiddich.api.domain.timetable;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TimePlace {
    String day;
    String start;
    String end;
    String place;
}
