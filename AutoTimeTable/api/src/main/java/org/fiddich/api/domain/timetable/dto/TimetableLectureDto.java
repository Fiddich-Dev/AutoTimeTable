package org.fiddich.api.domain.timetable.dto;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Lecture.Department;

import java.util.List;

@Getter
@AllArgsConstructor
public class TimetableLectureDto {

    private Long id;

    private String code;
    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String time;
    private String place;
    private String credit;
    private String target;
    private String notice;

    private Department department;

}
