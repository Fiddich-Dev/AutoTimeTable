package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "school_id")
    private School school;

    private String name;

    @Column(name = "`year`")
    private String year;

    private String semester;

    // 커스텀 강의 카테고리 만들기


    public Category(String year, String semester) {
        this.school = new School(13L, "성균관대학교");
        this.name = "커스텀강의";
        this.year = year;
        this.semester = semester;
    }

    public Category(String year, String semester, String name) {
        this.school = new School(13L, "성균관대학교");
        this.name = name;
        this.year = year;
        this.semester = semester;
    }
}
