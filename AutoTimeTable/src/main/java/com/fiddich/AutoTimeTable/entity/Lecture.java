package com.fiddich.AutoTimeTable.entity;

import jakarta.persistence.*;

@Entity
public class Lecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    private String code;
    private String codeSection;
    private String lectName;
    private String professor;
    private String lectTime;
    private String cmpDiv;
    private String credit;

}
