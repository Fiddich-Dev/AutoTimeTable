package org.fiddich.api.domain.timetable;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.dto.CreateTimetableWithExternalLecturesDto;
import org.fiddich.api.domain.timetable.dto.ExternalLectureDto;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

                Lecture newLecture = Lecture.builder()
                        .code(extDto.getCode())
                        .codeSection(codeSection)
                        .name(extDto.getName())
                        .professor(extDto.getProfessor())
                        .time(extDto.getTime())
                        .credit(extDto.getCredit())
                        .member(member)
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
    public List<CreateTimetableWithExternalLecturesDto> allEverytimeMapping(String url) throws IOException {

        // 에타 시간표id 추출
        String[] parts = url.split("/");
        String identifier = parts[parts.length - 1].replace("@", "");

        // 에타 서버에 요청 보내기
        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/table/friend")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://everytime.kr/")
                .data("identifier", identifier)
                .data("friendInfo", "true")
                .timeout(5000)
                .post();

        // 에타에 저장된 모든 시간표id, 학년도 정보
        Elements primaryTables = doc.select("primaryTable");
        // 조회할 시간표
        List<CreateTimetableWithExternalLecturesDto> createTimetableWithExternalLecturesDtos = new ArrayList<>();

        for (Element primaryTable : primaryTables) {

            String year = primaryTable.attr("year");
            String semester = primaryTable.attr("semester");
            identifier = primaryTable.attr("identifier");

            List<ExternalLectureDto> externalLectureDtos = getEveryTimetable(identifier);

            CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto = new CreateTimetableWithExternalLecturesDto();
            createTimetableWithExternalLecturesDto.setYear(year);
            createTimetableWithExternalLecturesDto.setSemester(semester);
            createTimetableWithExternalLecturesDto.setRepresent(false); // 일단 조회만 하니까 메인시간표로 설정X
            createTimetableWithExternalLecturesDto.setLectures(externalLectureDtos);

            createTimetableWithExternalLecturesDtos.add(createTimetableWithExternalLecturesDto);
        }
        return createTimetableWithExternalLecturesDtos;
    }


    // 특정 identifier로 시간표 조회
    public List<ExternalLectureDto> getEveryTimetable(String identifier) throws IOException {

        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/table/friend")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://everytime.kr/")
                .data("identifier", identifier)
                .data("friendInfo", "true")
                .timeout(5000)
                .post();

        Element tableElement = doc.selectFirst("table");
        Elements subjects = tableElement.select("subject");

        List<ExternalLectureDto> externalLectureDtos = new ArrayList<>();

        for (Element subject : subjects) {
            ExternalLectureDto externalLectureDto = new ExternalLectureDto();

            String subjectId = subject.attr("id");
            String fullCode = subject.selectFirst("internal").attr("value"); // codeSection
            String codePrefix = fullCode.split("-")[0]; // code

            Elements rawTimes = subject.selectFirst("time").select("data");
            StringBuilder time = new StringBuilder();
            for(Element rawTime : rawTimes) {
                String day = rawTime.attr("day");
                String start = rawTime.attr("starttime");
                String end = rawTime.attr("endtime");
                time.append(TimeParser.timeParse(day, start, end)).append(",");
            }
            if (!time.isEmpty()) {
                time.setLength(time.length() - 1); // 마지막 문자 제거 (예: ',' 제거)
            }
            String finalTime = time.toString(); // String으로 변환

            String professor = subject.selectFirst("professor").attr("value");
            String name = subject.selectFirst("name").attr("value");
            String credit = subject.selectFirst("credit").attr("value");

            externalLectureDto.setSubjectId(subjectId);
            externalLectureDto.setCode(codePrefix);
            externalLectureDto.setCodeSection(fullCode);
            externalLectureDto.setProfessor(professor);
            externalLectureDto.setName(name);
            externalLectureDto.setTime(finalTime);
            externalLectureDto.setCredit(credit);

            if(time == null || time.isEmpty()) {
                continue;
            }

            externalLectureDtos.add(externalLectureDto);
        }
        return externalLectureDtos;
    }

    public String numToDay(String num) {
        return switch (num) {
            case "0" -> "월";
            case "1" -> "화";
            case "2" -> "수";
            case "3" -> "목";
            case "4" -> "금";
            case "5" -> "토";
            case "6" -> "일";
            default -> throw new IllegalArgumentException("잘못된 요일: " + num);
        };
    }

    public String numToTime(String time) {
        int num = Integer.parseInt(time);
        int hour = num * 5 / 60;
        int minute = num * 5 % 60;
        return String.format("%d%02d", hour, minute);
    }

}
