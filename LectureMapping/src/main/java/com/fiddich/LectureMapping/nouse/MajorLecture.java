package com.fiddich.LectureMapping.nouse;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
public class MajorLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String professor;
    private String type;
    private String time;
    private String place;
    private String credit;
    private String target;
    private String notice;

    public MajorLecture(String code, String name, String professor, String type, String time, String place, String credit, String target, String notice) {
        this.code = code;
        this.name = name;
        this.professor = professor;
        this.type = type;
        this.time = time;
        this.place = place;
        this.credit = credit;
        this.target = target;
        this.notice = notice;
    }

    @Override
    public String toString() {
        return "MajorLecture{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", professor='" + professor + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", place='" + place + '\'' +
                ", credit='" + credit + '\'' +
                ", target='" + target + '\'' +
                ", notice='" + notice + '\'' +
                '}';
    }
}
