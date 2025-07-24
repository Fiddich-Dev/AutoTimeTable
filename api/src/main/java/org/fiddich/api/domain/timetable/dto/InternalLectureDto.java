package org.fiddich.api.domain.timetable.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;

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

        this.credit = lecture.getCredit();
        this.categoryName = lecture.getCategory() != null ? lecture.getCategory().getName() : null;
        this.notice = lecture.getNotice();

        StringBuilder sb = new StringBuilder();

        for(LectureTime lectureTime : lecture.getLectureTimes()) {
            String day = String.valueOf(lectureTime.getDay());
            String start = String.valueOf(lectureTime.getStart());
            String end = String.valueOf(lectureTime.getEnd());
            String time = TimeParser.timeParse(day, start, end);
            sb.append(time).append(",");
        }

        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1); // 마지막 쉼표 제거
        }

        this.time = sb.toString();
    }

} // 내 DB에서 강의를 조회할떄 사용
