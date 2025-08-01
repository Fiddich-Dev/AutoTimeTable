package org.fiddich.api.domain.everytime.dto;

import lombok.*;

import java.util.ArrayList;
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
    String place;
    String credit;
    String target;
    String notice;
    List<TimePlace> timeplaceList = new ArrayList<>();

    public String getSubjectCode() {
        return this.code.split("-")[0];
    }
}