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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
