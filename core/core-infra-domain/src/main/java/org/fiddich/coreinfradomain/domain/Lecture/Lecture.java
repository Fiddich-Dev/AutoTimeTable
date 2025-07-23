package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.Member;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String code;
    private String codeSection;
    private String name;
    private String professor;
    private String type;

//    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<LectureTime> lectureTimes = new ArrayList<>();

    private String time;

    private String place;
    private String credit;
    private String target;
    private String notice;


//    public void addLectureTime(LectureTime lectureTime) {
//        this.lectureTimes.add(lectureTime);
//        lectureTime.setLecture(this);
//    }
}
