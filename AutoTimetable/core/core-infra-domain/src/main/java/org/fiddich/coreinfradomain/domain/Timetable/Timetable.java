package org.fiddich.coreinfradomain.domain.Timetable;

import jakarta.persistence.*;
import lombok.*;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.TimetableLecture;

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

    // member객체를 넣고 그 member의 시간표에 추가한다
    public void setMember(Member member) {
        this.member = member;
//        member.getTimetables().add(this);
    }

    public void setTimetableLectures(List<TimetableLecture> timetableLectures) {
        this.timetableLectures.clear(); // 기존 목록 비우기
        for (TimetableLecture l : timetableLectures) {
            l.setTimetable(this);       // 역참조만 설정
            this.timetableLectures.add(l); // 새 목록에 추가
        }
    }

    public void setRepresent(Boolean represent) {
        isRepresent = represent;
    }
}
