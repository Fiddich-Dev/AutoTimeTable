package com.fiddich.LectureMapping.entity;

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

    @Column(name = "`year`")
    private String year;
    private String semester;
    private boolean isCustom;

//    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

//    // 외부 API에서 받은 lectureId를 저장할 별도 필드 추가
//    @Column(name = "external_lecture_id", unique = true)
//    private Long externalLectureId;

//    public Lecture(String code, String codeSection, String name, String professor, String type, String time, String place, String credit, String target, String notice, String year, String semester, boolean isCustom, Long memberId, School school, Department department) {
//        this.code = code;
//        this.codeSection = codeSection;
//        this.name = name;
//        this.professor = professor;
//        this.type = type;
//        this.time = time;
//        this.place = place;
//        this.credit = credit;
//        this.target = target;
//        this.notice = notice;
//        this.year = year;
//        this.semester = semester;
//        this.isCustom = isCustom;
//        this.memberId = memberId;
//        this.school = school;
//        this.department = department;
//    }

//    @Override
//    public String toString() {
//        return "Lecture{" +
//                "id=" + id +
//                ", code='" + code + '\'' +
//                ", codeSection='" + codeSection + '\'' +
//                ", name='" + name + '\'' +
//                ", professor='" + professor + '\'' +
//                ", type='" + type + '\'' +
//                ", time='" + time + '\'' +
//                ", place='" + place + '\'' +
//                ", credit='" + credit + '\'' +
//                ", target='" + target + '\'' +
//                ", notice='" + notice + '\'' +
//                ", year='" + year + '\'' +
//                ", semester='" + semester + '\'' +
//                ", isCustom=" + isCustom +
//                ", memberId=" + memberId +
//                ", school=" + school +
//                ", department=" + department +
//                '}';
//    }
}
