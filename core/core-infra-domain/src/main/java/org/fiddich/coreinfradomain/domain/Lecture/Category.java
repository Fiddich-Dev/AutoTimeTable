package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="category")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @Column(name = "category_id")
    private Long id;

    private String name;

    private int orderNum;

    @Column(name = "`year`")
    private String year;      // 추가된 필드

    private String semester;  // 추가된 필드

    private Long campusId;  // 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    public Category(Long id, String name, int orderNum, String year, String semester) {
        this.id = id;
        this.name = name;
        this.orderNum = orderNum;
        this.year = year;
        this.semester = semester;
    }

    public Category(Long id, String name, int orderNum) {
        this.id = id;
        this.name = name;
        this.orderNum = orderNum;
    }
}
