package org.fiddich.api.domain.timetable;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Subject {
    String id;
    String code;
    String name;
    String professor;
    String type;
    String time;
    String place;
    String credit;
    String capacity;
    String popular;
    String target;
    String notice;
    String lectureId;
    String lectureRate;
    List<TimePlace> timeplaceList;
}