//package com.fiddich.LectureMapping.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//public class Department {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "department_id")
//    private Long id;
//
//    private String name;
//    private String code;
//
//    @ManyToOne
//    @JoinColumn(name = "school_id")
//    private School school;
//
//}
