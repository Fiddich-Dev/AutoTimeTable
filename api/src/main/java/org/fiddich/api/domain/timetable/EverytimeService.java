package org.fiddich.api.domain.timetable;

import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.dto.CreateTimetableWithExternalLecturesDto;
import org.fiddich.api.domain.timetable.dto.ExternalLectureDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.fiddich.api.domain.timetable.helper.EverytimeRequester;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;
import org.fiddich.coreinfradomain.domain.Lecture.repository.LectureRepository;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.fiddich.coreinfradomain.domain.Timetable.repository.TimetableRepository;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EverytimeService {

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;
    private final LectureRepository lectureRepository;

    // 에타 시간표를 내 DB에 저장하기(내 DB에 없는 강의는 강의DB에 먼저 저장하고 저장)
    public void createTimetableWithExternalLectures(CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        String year = createTimetableWithExternalLecturesDto.getYear();
        String semester = createTimetableWithExternalLecturesDto.getSemester();
        String timetableName = createTimetableWithExternalLecturesDto.getTimeTableName();
        boolean isRepresent = createTimetableWithExternalLecturesDto.isRepresent();

        if(isRepresent) {
            timetableRepository.clearMainTimetable(member.getId());
        }

        Timetable timetable = Timetable.builder()
                .member(member)
                .year(year)
                .semester(semester)
                .timeTableName(timetableName)
                .isRepresent(isRepresent)
                .build();

        List<Lecture> lectures = new ArrayList<>();

        for (ExternalLectureDto extDto : createTimetableWithExternalLecturesDto.getLectures()) {
            // 1. 내부 DB에서 같은 강의 있는지 확인
            Optional<Lecture> optionalLecture = lectureRepository.findByCodeSection(extDto.getCodeSection(), year, semester);

            // 2. subjectId(codeSection)으로 조회 시도
            if (optionalLecture.isEmpty()) {
                optionalLecture = lectureRepository.findByCodeSection(extDto.getSubjectId(), year, semester);
            }

            Lecture lecture = optionalLecture.orElseGet(() -> {
                // 없다면 새로 저장
                System.out.println("없는 강의");
                String codeSection = extDto.getSubjectId();

                List<LectureTime> lectureTimes = new ArrayList<>();
                String[] dayAndTimes = extDto.getTime().split(",");
                for(String dayAndTime : dayAndTimes) {
                    lectureTimes.add(TimeParser.timeParse(dayAndTime));
                }

                Lecture newLecture = Lecture.builder()
                        .code(extDto.getCode())
                        .codeSection(codeSection)
                        .name(extDto.getName())
                        .professor(extDto.getProfessor())
                        .credit(extDto.getCredit())
                        .member(member)
                        .lectureTimes(lectureTimes)
                        .build();

                lectureRepository.save(newLecture);

                return newLecture;
            });

            lectures.add(lecture);
        }

        timetable.changeLectures(lectures);
        timetableRepository.save(timetable);
    }

    // 에타의 모든 시간표 가져오기(조회만)
    public List<CreateTimetableWithExternalLecturesDto> allEverytimeMapping(String url) {
        try {
            return EverytimeRequester.findAllEveryTimetable(url);
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 모든 학과 가져오기 (상위학과 제외)
    public List<Category> everytimeCategories(String year, String semester) {
        try {
            return EverytimeRequester.findAllCategories(year, semester);
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 학과번호로 페이징된 강의 찾기
    public List<InternalLectureDto> getLecturesByCategoryId(String categoryId, String year, String semester) {
        try {
            List<Subject> subjects = EverytimeRequester.findSubjectsByCategoryId(categoryId, year, semester);
            return subjects.stream().map(InternalLectureDto::new).toList();
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

    // 키워드로 강의 검색하기
    public List<InternalLectureDto> searchEverytimeLectures(String keywordJson, String year, String semester, int page, int size) {
        try {
            List<Subject> subjects = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, size, page*size);
            return subjects.stream().map(InternalLectureDto::new).toList();
        } catch (IOException e) {
            log.error("everytime 요청 에러");
            return Collections.emptyList();
        }
    }

}
