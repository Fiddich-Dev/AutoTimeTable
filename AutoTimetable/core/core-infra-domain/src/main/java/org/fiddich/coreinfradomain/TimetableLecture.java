package org.fiddich.coreinfradomain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

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

    // 나중에 전공관련 가중치 줄 수 있음

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
        // ❌ timetable.getTimetableLectures().add(this); <- 이거 절대 넣지 마세요!
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }
}
