package com.fiddich.AutoTimeTable.entity;

import jakarta.persistence.*;

import java.sql.Time;

@Entity
public class TimetableLecture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetableLecture_id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    // 나중에 전공관련 가중치 줄 수 있음

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
        timetable.getTimetableLectures().add(this);
    }

}
