package com.fiddich.AutoTimeTable.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Timetable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String semesterYear;
    private String timeTableName;
    private Boolean isRepresent;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL)
    private List<TimetableLecture> timetableLectures;

    // member객체를 넣고 그 member의 시간표에 추가한다
    public void setMember(Member member) {
        this.member = member;
        member.getTimetables().add(this);
    }

}
