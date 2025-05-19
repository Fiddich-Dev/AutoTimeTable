package com.fiddich.LectureMapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Lecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    private String code;
    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String time;
    private String place;
    private String credit;
    private String target;
    private String notice;
    private String department;

    public Lecture(String code, String codeSection, String name, String professor, String type, String time, String place, String credit, String target, String notice, String department) {
        this.code = code;
        this.codeSection = codeSection;
        this.name = name;
        this.professor = professor;
        this.type = type;
        this.time = time;
        this.place = place;
        this.credit = credit;
        this.target = target;
        this.notice = notice;
        this.department = department;
    }

    @Override
    public String toString() {
        return "Lecture{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", codeSection='" + codeSection + '\'' +
                ", name='" + name + '\'' +
                ", professor='" + professor + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", place='" + place + '\'' +
                ", credit='" + credit + '\'' +
                ", target='" + target + '\'' +
                ", notice='" + notice + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
