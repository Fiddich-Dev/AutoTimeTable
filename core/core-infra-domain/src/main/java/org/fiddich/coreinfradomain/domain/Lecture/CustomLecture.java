package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Entity
@DiscriminatorValue("CUSTOM")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomLecture extends Lecture {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "lecture_id")
//    private Long id;

    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    @Builder.Default
    @OneToMany(mappedBy = "customLecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LectureTime> lectureTimes = new ArrayList<>();

//    private String codeSection;
    private String name;
    private String professor;
    private String type;
    private String place;
    private String credit;
    private String target;
    private String notice;

    public void addLectureTime(LectureTime lectureTime) {
        this.lectureTimes.add(lectureTime);
        lectureTime.setCustomLecture(this);
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public void addLectureTime(List<LectureTime> lectureTimes) {
//        this.lectureTimes.clear();
        this.lectureTimes.addAll(lectureTimes);
        for(LectureTime lectureTime : lectureTimes) {
            lectureTime.setCustomLecture(this);
        }
    }

//    // 중복 제거된 리스트를 반환하는 메서드 추가
//    public List<LectureTime> getDistinctLectureTimes() {
//        return lectureTimes.stream()
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toMap(
//                                LectureTime::getId,
//                                Function.identity(),
//                                (existing, replacement) -> existing
//                        ),
//                        map -> new ArrayList<>(map.values())
//                ));
//    }

}
