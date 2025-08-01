package org.fiddich.api.domain.timetable.dto.request;

import lombok.Data;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomLectureDto {

    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String place;
    private String credit;
    private String target;
    private String notice;
    private String time;

    public List<LectureTime> decodeTime() {
        List<LectureTime> result = new ArrayList<>();
        String[] times = this.time.split("-");
        for(String t : times) {
            result.add(TimeParser.timeParse(t));
        }
        return result;
    }
}
