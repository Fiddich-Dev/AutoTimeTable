package org.fiddich.coreinfradomain.domain.Timetable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.TimetableLecture;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Timetable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String semesterYear;
    private String timeTableName;
    private Boolean isRepresent;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL)
    private List<TimetableLecture> timetableLectures;

    // member객체를 넣고 그 member의 시간표에 추가한다
    public void setMember(Member member) {
        this.member = member;
        member.getTimetables().add(this);
    }

}
