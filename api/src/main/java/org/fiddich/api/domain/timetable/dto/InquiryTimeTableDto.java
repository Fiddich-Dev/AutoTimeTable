package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fiddich.api.domain.everytime.EverytimeRequester;
import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
public class InquiryTimeTableDto {

    private Long id;
    private String year;
    private String semester;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<InternalLectureDto> lectures = new ArrayList<>();

    public boolean isRepresent() {
        return isRepresent;
    }


    public InquiryTimeTableDto(Timetable timetable) {
        this.id = timetable.getId();
        this.year = timetable.getYear();
        this.semester = timetable.getSemester();
        this.isRepresent = timetable.getIsRepresent();

        // 커스텀 강의 넣기
        this.lectures.addAll(timetable.getCustomLectures().stream().map(InternalLectureDto::new).toList());

        // 공식 강의 넣기
        List<String> officialLectures = timetable.getOfficialLectures().stream().map(ol -> ol.getCodeSection()).toList();
        for(String officialLecture : officialLectures) {
            String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", officialLecture);
            try {
                Subject findSubject = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, 50, 0).get(0);
                this.lectures.add(new InternalLectureDto(findSubject));
            } catch (IOException e) {
                System.out.println("에타 api 조회 못함");
            }
        }
    }

} // 조회기능만 한다, 하지만 id로 클라이언트에서 수정요청을 날릴 수 있다
