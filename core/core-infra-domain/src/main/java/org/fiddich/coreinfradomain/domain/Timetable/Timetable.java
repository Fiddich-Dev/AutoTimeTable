package org.fiddich.coreinfradomain.domain.Timetable;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Member.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "timetable_tb")
public class Timetable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "year_col")
    private String year;

    private String semester;
    private String timeTableName;
    private Boolean isRepresent;

    @Builder.Default
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimetableLecture> timetableLectures = new ArrayList<>();



    public void setMember(Member member) {
        this.member = member;
    }

    public void changeLectures(List<Lecture> lectures) {
        this.timetableLectures.clear();
        for(Lecture lecture : lectures) {
            this.timetableLectures.add(new TimetableLecture(this, lecture));
        }
    }

}
