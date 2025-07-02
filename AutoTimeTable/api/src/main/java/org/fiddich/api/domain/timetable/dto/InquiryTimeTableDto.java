package org.fiddich.api.domain.timetable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class InquiryTimeTableDto {

    private Long id;
    private String year;
    private String semester;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<InternalLectureDto> lectures;

    public boolean isRepresent() {
        return isRepresent;
    }
} // 조회기능만 한다, 하지만 id로 클라이언트에서 수정요청을 날릴 수 있다
