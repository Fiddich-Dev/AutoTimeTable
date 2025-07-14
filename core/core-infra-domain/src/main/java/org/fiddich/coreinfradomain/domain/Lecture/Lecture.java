package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.Member;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

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
}
