package org.fiddich.api.domain.timetable.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;

@Getter
@AllArgsConstructor
public class InternalLectureDto {

    private Long id;

    private String code;
    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String time;
    private String credit;
    private String categoryName;
    private String notice;

    public InternalLectureDto(Lecture lecture) {
        this.id = lecture.getId();
        this.code = lecture.getCode();
        this.codeSection = lecture.getCodeSection();
        this.name = lecture.getName();
        this.professor = lecture.getProfessor();
        this.type = lecture.getType();
        this.time = lecture.getTime();
        this.credit = lecture.getCredit();
        this.categoryName = lecture.getCategory() != null ? lecture.getCategory().getName() : null;
        this.notice = lecture.getNotice();
    }

} // 내 DB에서 강의를 조회할떄 사용
