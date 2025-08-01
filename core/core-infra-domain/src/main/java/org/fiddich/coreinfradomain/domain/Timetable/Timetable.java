package org.fiddich.coreinfradomain.domain.Timetable;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Lecture.CustomLecture;
import org.fiddich.coreinfradomain.domain.Lecture.OfficialLecture;
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

    @Builder.Default
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfficialLecture> officialLectures = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomLecture> customLectures = new ArrayList<>();

    @Column(name = "year_col")
    private String year;

    private String semester;
    private String timeTableName;
    private Boolean isRepresent;


    public void setMember(Member member) {
        this.member = member;
    }

    public void changeLectures(List<OfficialLecture> officialLectures, List<CustomLecture> customLectures) {
        this.officialLectures.clear();
        this.customLectures.clear();
        this.officialLectures.addAll(officialLectures);
        this.customLectures.addAll(customLectures);

        for(OfficialLecture officialLecture : officialLectures) {
            officialLecture.setTimetable(this);
        }

        for(CustomLecture customLecture : customLectures) {
            customLecture.setTimetable(this);
        }
    }

}
