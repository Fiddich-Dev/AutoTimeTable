package org.fiddich.api.domain.timetable;

import jakarta.persistence.EntityNotFoundException;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.coreinfradomain.TimetableLecture;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.repository.LectureRepository;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.Timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;
    private final LectureRepository lectureRepository;

    public Long save(TimetableDto timetableDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Timetable timetable = Timetable.builder()
                .member(member)
                        .year(timetableDto.getYear())
                                .semester(timetableDto.getSemester())
                .timeTableName(timetableDto.getTimeTableName())
                .isRepresent(timetableDto.getIsRepresent())
                .build();

        // TimetableLecture 생성
        List<TimetableLecture> timetableLectures = timetableDto.getSelectedLectureIds().stream()
                .map(lectureId -> {
                    Lecture lecture = lectureRepository.findById(lectureId)
                            .orElseThrow(() -> new NoSuchElementException("해당 강의가 존재하지 않습니다."));
                    return TimetableLecture.builder()
                            .timetable(timetable)
                            .lecture(lecture)
                            .build();
                }).collect(Collectors.toList());

        timetable.setTimetableLectures(timetableLectures); // 연관관계 설정

        timetableRepository.save(timetable);

        return timetable.getId();
    }

    public List<YearAndSemesterDto> getYearAndSemester() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Timetable> timetables = timetableRepository.findByMember(customUserDetails.getId());
        return timetables.stream()
                .map(t -> new YearAndSemesterDto(t.getYear(), t.getSemester()))
                .distinct()
                .toList();
    }

    public List<TimeTableDto2> getTimetablesWithLectures(String year, String semester) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Timetable> timetables = timetableRepository.findTimetablesWithLecturesByMemberId(customUserDetails.getId());
        return timetables.stream()
                .filter(t -> t.getYear().equals(year) && t.getSemester().equals(semester)) // 여기 수정
                .map(t -> new TimeTableDto2(
                        t.getId(),
                        t.getYear(),
                        t.getSemester(),
                        t.getTimeTableName(),
                        Boolean.TRUE.equals(t.getIsRepresent()),
                        t.getTimetableLectures().stream()
                                .map(l -> new TimetableLectureDto(l.getLecture().getId(), l.getLecture().getCode(), l.getLecture().getCodeSection(), l.getLecture().getName(), l.getLecture().getProfessor(), l.getLecture().getType(), l.getLecture().getTime(), l.getLecture().getPlace(), l.getLecture().getCredit(), l.getLecture().getTarget(), l.getLecture().getNotice(), l.getLecture().getDepartment()))
                                .toList()
                ))
                .toList();

    }

    public void editTimetable(Long timetableId, LectureDto lectureDto) {

        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new NoSuchElementException("시간표를 찾을 수 없습니다."));

        timetable.getTimetableLectures().clear();

        List<Lecture> lectures = new ArrayList<>();

        List<Long> lectureIds = lectureDto.getLectureIds();

        for(Long lectureId : lectureIds) {
            Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new NoSuchElementException("강의 찾을 수 없음"));
            TimetableLecture timetableLecture = new TimetableLecture();
            timetableLecture.setTimetable(timetable);
            timetableLecture.setLecture(lecture);
            timetable.getTimetableLectures().add(timetableLecture);
        }

    }

    public void deleteTimetable(Long timetableId) {
        timetableRepository.deleteTimetable(timetableId);
    }

    public List<Lecture> getAllLectures() {
        return lectureRepository.findAll();
    }




//    public Timetable findRepresentTimetable(Long memberId) {
//        List<Timetable> timetables = findAllTimetable(memberId);
//        // null이 나올수도 있음 고쳐야함
//        return timetables.stream().filter(t -> t.getIsRepresent()).findFirst().get();
//    }

//    public List<Timetable> findAllTimetable(Long memberId) {
//        return memberRepository.findById(memberId).getTimetables();
//    }
}
