package org.fiddich.coreinfradomain.domain.Lecture;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@DiscriminatorValue("OFFICIAL")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialLecture extends Lecture {


    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "lecture_id")
//    private Long id;
//
//    private String codeSection;


    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }
}
