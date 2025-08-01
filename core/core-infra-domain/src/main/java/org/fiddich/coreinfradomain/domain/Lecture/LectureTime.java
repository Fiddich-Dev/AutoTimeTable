package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LectureTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_time_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_lecture_id")
    private CustomLecture customLecture;

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
