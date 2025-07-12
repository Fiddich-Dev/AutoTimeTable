package org.fiddich.api.domain.timetable.dto;

import lombok.Data;

@Data
public class ExternalLectureDto {

    private String subjectId;
    private String code;
    private String codeSection;
    private String name;
    private String professor;
    private String time;
    private String credit;
} // 외부 DB에서 강의를 조회할떄 사용
