//package org.fiddich.coreinfradomain.domain.Timetable;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.fiddich.coreinfradomain.domain.Lecture.OfficialLecture;
//
//@Entity
//@Builder
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//public class TimetableOfficialLecture {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "timetable_official_lecture_id")
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "timetable_id")
//    private Timetable timetable;
//
//    @ManyToOne
//    @JoinColumn(name = "official_lecture_id")
//    private OfficialLecture officialLecture;
//
//
//    public void setTimetable(Timetable timetable) {
//        this.timetable = timetable;
//    }
//
//    public void setOfficialLecture(OfficialLecture officialLecture) {
//        this.officialLecture = officialLecture;
//    }
//
//    public TimetableOfficialLecture(Timetable timetable, OfficialLecture officialLecture) {
//        this.timetable = timetable;
//        this.officialLecture = officialLecture;
//        // 양방향 매핑
//        timetable.getTimetableOfficialLectures().add(this);
//    }
//}
