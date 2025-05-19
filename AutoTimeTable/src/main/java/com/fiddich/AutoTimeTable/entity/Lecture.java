package com.fiddich.AutoTimeTable.entity;

import jakarta.persistence.*;

@Entity
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

    @Enumerated(EnumType.STRING)
    private Department department;

}
