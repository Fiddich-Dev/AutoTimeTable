package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LectureTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_time_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(name = "day_of_week")
    private Integer day;
    @Column(name = "start_time")
    private Integer start;
    @Column(name = "end_time")
    private Integer end;

    public LectureTime(Integer day, Integer start, Integer end) {
        this.day = day;
        this.start = start;
        this.end = end;
    }
}
