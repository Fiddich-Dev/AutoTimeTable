package org.fiddich.api.domain.everytime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TimetableByUrl {

    private String year;
    private String semester;
    private String timeTableName;

    @JsonProperty("isRepresent")
    private boolean isRepresent;

    private List<Subject> subjects = new ArrayList<>();

    public boolean isRepresent() {
        return isRepresent;
    }
}
