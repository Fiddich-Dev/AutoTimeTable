package org.fiddich.coreinfradomain.domain.Timetable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TimetableLecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetableLecture_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;


    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public TimetableLecture(Timetable timetable, Lecture lecture) {
        this.timetable = timetable;
        this.lecture = lecture;
        // 양방향 매핑
        timetable.getTimetableLectures().add(this);
    }
}
