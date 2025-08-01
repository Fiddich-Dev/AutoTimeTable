package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.api.domain.everytime.dto.TimePlace;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.coreinfradomain.domain.Lecture.CustomLecture;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;
import org.fiddich.coreinfradomain.domain.Lecture.OfficialLecture;

import java.util.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InternalLectureDto {

    private Long id;

    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String credit;
    private String notice;
    private String time;

    public List<LectureTime> decodeTime() {
        List<LectureTime> result = new ArrayList<>();
        String[] times = this.time.split(",");
        for(String t : times) {
            result.add(TimeParser.timeParse(t));
        }
        return result;
    }

    public InternalLectureDto(Subject subject) {
        this.id = Long.parseLong(subject.getId());
        this.codeSection = subject.getCode();
        this.name = subject.getName();
        this.professor = subject.getProfessor();
        this.type = subject.getType();
        this.credit = subject.getCredit();
        this.notice = subject.getNotice();

        StringBuilder sb = new StringBuilder();

        for(TimePlace timePlace : subject.getTimeplaceList()) {
            String day = String.valueOf(timePlace.getDay());
            String start = String.valueOf(timePlace.getStart());
            String end = String.valueOf(timePlace.getEnd());
            String time = TimeParser.timeParse(day, start, end);
            sb.append(time).append(",");
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1); // 마지막 쉼표 제거
        }
        this.time = sb.toString();
    }

    public InternalLectureDto(CustomLecture customLecture) {

        this.id = customLecture.getId();
        this.codeSection = customLecture.getCodeSection();
        this.name = customLecture.getName();
        this.professor = customLecture.getProfessor();
        this.type = customLecture.getType();
        this.credit = customLecture.getCredit();
        this.notice = customLecture.getNotice();

        for(LectureTime lectureTime : customLecture.getLectureTimes()) {
            System.out.println(lectureTime);
        }

        // 중복 제거 로직
        Set<String> uniqueTimes = new LinkedHashSet<>(); // 순서 유지

        for(LectureTime lectureTime : customLecture.getLectureTimes()) {
            String day = String.valueOf(lectureTime.getDay());
            String start = String.valueOf(lectureTime.getStart());
            String end = String.valueOf(lectureTime.getEnd());
            String time = TimeParser.timeParse(day, start, end);
            uniqueTimes.add(time); // Set이 자동으로 중복 제거
        }

        this.time = String.join(",", uniqueTimes);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InternalLectureDto that = (InternalLectureDto) o;
        return Objects.equals(codeSection, that.codeSection) && Objects.equals(name, that.name) && Objects.equals(professor, that.professor) && Objects.equals(type, that.type) && Objects.equals(credit, that.credit) && Objects.equals(notice, that.notice) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeSection, name, professor, type, credit, notice, time);
    }
} // 내 DB에서 강의를 조회할떄 사용
