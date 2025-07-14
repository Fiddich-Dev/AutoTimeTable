package org.fiddich.api.domain.timetable;

import jakarta.persistence.EntityManager;
import org.fiddich.api.domain.lecture.dto.InquiryDepartmentDto;
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
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;
    private final LectureRepository lectureRepository;
    private final EntityManager em;

    public Long save(CreateTimetableDto createTimetableDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        String year = createTimetableDto.getYear();
        String semester = createTimetableDto.getSemester();
        String timetableName = createTimetableDto.getTimeTableName();
        boolean isRepresent = createTimetableDto.getIsRepresent();
        List<Long> lectureIds = createTimetableDto.getSelectedLectureIds();
        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIds);


        if(isRepresent) {
            List<Timetable> timetables = timetableRepository.findByMember(member.getId());
            for(Timetable timetable : timetables) {
                timetable.setRepresent(false);
            }
        }


        Timetable timetable = Timetable.builder()
                .member(member)
                .year(year)
                .semester(semester)
                .timeTableName(timetableName)
                .isRepresent(isRepresent)
                .build();

        List<TimetableLecture> timetableLectures = new ArrayList<>();

        for(Lecture lecture : lectures) {
            TimetableLecture timetableLecture = TimetableLecture.builder()
                    .timetable(timetable)
                    .lecture(lecture)
                    .build();

            timetableLectures.add(timetableLecture);
        }

        timetable.setTimetableLectures(timetableLectures); // 연관관계 설정
        timetableRepository.save(timetable);
        return timetable.getId();
    }
    // 완료
    public List<YearAndSemesterDto> getYearAndSemester() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Timetable> timetables = timetableRepository.findByMember(customUserDetails.getId());
        return timetables.stream()
                .map(t -> new YearAndSemesterDto(t.getYear(), t.getSemester()))
                .distinct()
                .toList();
    }
    // 완료
    public List<InquiryTimeTableDto> getTimetablesAboutYearAndSemester(String year, String semester) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Timetable> timetables = timetableRepository.findTimetablesWithLecturesByMemberId(customUserDetails.getId());
        return timetables.stream()
                .filter(t -> t.getYear().equals(year) && t.getSemester().equals(semester))
                .map(t -> new InquiryTimeTableDto(
                        t.getId(),
                        t.getYear(),
                        t.getSemester(),
                        t.getIsRepresent(),
                        t.getTimetableLectures().stream()
                                .map(l -> new InternalLectureDto(l.getLecture().getId(), l.getLecture().getCode(), l.getLecture().getCodeSection(), l.getLecture().getName(), l.getLecture().getProfessor(), l.getLecture().getType(), l.getLecture().getTime(), l.getLecture().getCredit(), l.getLecture().getCategory().getName(), l.getLecture().getNotice()))
                                .toList()
                ))
                .toList();
    }
    // 완료
    public InquiryTimeTableDto getMainTimetableWithLectures(String year, String semester) {
        // 학년도로 시간표 조회 메서드 사용
        List<InquiryTimeTableDto> findInquiryTimeTableDtos = getTimetablesAboutYearAndSemester(year, semester);
        // 만약 메인사간표가 없다면 null을 반환
        return findInquiryTimeTableDtos.stream().filter(t -> t.isRepresent() == true).findFirst().orElse(null);
    }
    // 일단 완료
    public void editTimetable(Long timetableId, LectureIdsDto lectureIdsDto) {
        // timetableLecture중에 dto에 없는거만 삭제
        // dto중에 timetableLecture에 없는거만 삽입
        // 배치사이즈 설정

        // 조회
        Timetable timetable = timetableRepository.findByIdWithTimetableLectures(timetableId)
                .orElseThrow(() -> new NoSuchElementException("시간표를 찾을 수 없습니다."));
        // 삭제
        timetableRepository.deleteTimetableLecture(timetableId);
        // 조회
        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIdsDto.getLectureIds());
        // 삽입
        for(Lecture lecture : lectures) {
            TimetableLecture timetableLecture = new TimetableLecture();
            timetableLecture.setTimetable(timetable);
            timetableLecture.setLecture(lecture);
            timetable.getTimetableLectures().add(timetableLecture);
        }
    }
    // 완료
    public void deleteTimetable(Long timetableId) {
        timetableRepository.deleteTimetable(timetableId);
    }
    // 검색 구조 변경
    public List<Lecture> getAllLectures() {
        return lectureRepository.findAll();
    }



    // 완료
    public void changeMainTimetable(TimetableIdDto timetableIdDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = customUserDetails.getId();
        Long timetableId = timetableIdDto.getTimetableId();

        timetableRepository.clearMainTimetable(memberId);
        timetableRepository.updateMainTimetable(timetableId);
    }

    // 테스트 필요
    public List<List<InternalLectureDto>> createTimetable(CreateTimetableOptionDto optionDto) {
        int targetMajorCnt = optionDto.getTargetMajorCnt();
        int targetCultureCnt = optionDto.getTargetCultureCnt();
        List<Long> dislikeLectureCodes = optionDto.getDislikeLectureCode();
        List<Long> likeLectureCodes = optionDto.getLikeLectureCode();
        List<Long> categoryIds = optionDto.getCategoryIds();
        int[][] usedTime = optionDto.getUsedTime();

        // 고른 전공들의 강의 찾기
        List<Lecture> lectures = lectureRepository.findAllByCategoryIdsWithParentCategory(categoryIds);
        // 포함시킬 강의들
        List<Lecture> likeLectures = lectureRepository.findAllByIds(likeLectureCodes);
        // 제외시킬 강의들
        List<Lecture> dislikeLecture = lectureRepository.findAllByIds(dislikeLectureCodes);

        int includedMajorCnt = 0;
        int includedCultureCnt = 0;

        // 포함시킬 강의중에 교양이 몇개인지 찾아야함
        for(Lecture lecture : likeLectures) {
            if(lecture.getCategory().getParent().getName().equals("교양/기타")) {
                includedCultureCnt++;
            }
            else {
                includedMajorCnt++;
            }
        }

        TimetableGenerator timetableGenerator = new TimetableGenerator(lectures);
        timetableGenerator.create(targetMajorCnt, targetCultureCnt, likeLectures, dislikeLecture, includedMajorCnt, includedCultureCnt, usedTime);

        TimetableScorer scorer = new TimetableScorer(optionDto.isPreferMorning(), optionDto.isPreferAfternoon());

        List<List<Lecture>> sortedTimetable = timetableGenerator.getMakedTimeTable(optionDto.getMinCredit(), optionDto.getMaxCredit()).stream()
                .sorted(Comparator.comparingInt(scorer::score).reversed()) // 점수 높은 게 좋은 시간표
                .collect(Collectors.toList());

        return sortedTimetable.stream()
                .map(innerList ->
                        innerList.stream()
                                .map(l -> new InternalLectureDto(l.getId(), l.getCode(), l.getCodeSection(), l.getName(), l.getProfessor(), l.getType(), l.getTime(), l.getCredit(), l.getCategory().getName(), l.getNotice())) // 각 Lecture → InternalLectureDto로 변환
                                .collect(Collectors.toList())
                )
                .collect(Collectors.toList());

    }

    // 에타 시간표를 내 DB에 저장하기(내 DB에 없는 강의는 강의DB에 먼저 저장하고 저장)
    public void createTimetableWithExternalLectures(CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        String year = createTimetableWithExternalLecturesDto.getYear();
        String semester = createTimetableWithExternalLecturesDto.getSemester();
        String name = createTimetableWithExternalLecturesDto.getTimeTableName();
        boolean isRepresent = createTimetableWithExternalLecturesDto.isRepresent();

        if(isRepresent) {
            List<Timetable> timetables = timetableRepository.findByMember(member.getId());
            for(Timetable timetable : timetables) {
                timetable.setRepresent(false);
            }
        }

        Timetable timetable = Timetable.builder()
                .member(member)
                .year(year)
                .semester(semester)
                .timeTableName(name)
                .isRepresent(isRepresent)
                .build();



        List<TimetableLecture> timetableLectures = new ArrayList<>();

        for (ExternalLectureDto extDto : createTimetableWithExternalLecturesDto.getLectures()) {
            // 1. 내부 DB에서 같은 강의 있는지 확인
            Optional<Lecture> optionalLecture = lectureRepository.findByCodeSection(extDto.getCodeSection());

            // 2. codeSection으로 없으면 subjectId로 조회 시도
            if (optionalLecture.isEmpty()) {
                optionalLecture = lectureRepository.findByCodeSection(extDto.getSubjectId());
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
                        .build();

                lectureRepository.save(newLecture);

                return newLecture;
            });

            // 2. 시간표에 매핑
            TimetableLecture tl = new TimetableLecture();
            tl.setTimetable(timetable);
            tl.setLecture(lecture);
            timetableLectures.add(tl);
        }

        timetable.setTimetableLectures(timetableLectures);
        timetableRepository.save(timetable);

    }


    // 에타의 모든 시간표 가져오기(조회만)
    public List<CreateTimetableWithExternalLecturesDto> allEverytimeMapping(String url) throws IOException {
        // 사용자 조회
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

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
                .post(); // ← 여기 수정

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
                .post(); // ← 여기 수정

        Element tableElement = doc.selectFirst("table");
        String year = tableElement.attr("year");
        String semester = tableElement.attr("semester");
        Elements subjects = tableElement.select("subject");

        List<ExternalLectureDto> externalLectureDtos = new ArrayList<>();

        for (Element subject : subjects) {
            String subjectId = subject.attr("id"); // 가장 기본적인 방법

            ExternalLectureDto lecture = new ExternalLectureDto();

            String fullCode = subject.selectFirst("internal").attr("value"); // codeSection
            String codePrefix = fullCode.split("-")[0]; // code
            String rawTime = subject.selectFirst("time").attr("value"); // 변환전 시간
            String time = TimeParser.timeParse(rawTime);

            lecture.setCode(codePrefix);
            lecture.setCodeSection(fullCode);
            lecture.setProfessor(subject.selectFirst("professor").attr("value"));
            lecture.setName(subject.selectFirst("name").attr("value"));
            lecture.setTime(time);
            lecture.setCredit(subject.selectFirst("credit").attr("value"));

            lecture.setSubjectId(subjectId);

            // 필요 시 추가 필드 매핑
            externalLectureDtos.add(lecture);
            System.out.println(lecture.toString());
        }
        return externalLectureDtos;
    }



    // 겹치는 강의만 가져오기
    public List<CompareTimetableDto> compareTimetable(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = customUserDetails.getId();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        // 🔹 내 대표 시간표에서 강의 목록 조회 (JPQL)
        List<Lecture> myLectures = em.createQuery("""
        select l from Timetable t
        join t.timetableLectures tl
        join tl.lecture l
        where t.member.id = :memberId
        and t.isRepresent = true
        and t.year = :year
        and t.semester = :semester
    """, Lecture.class)
                .setParameter("memberId", myId)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getResultList();

        // 🔹 결과 초기화
        List<CompareTimetableDto> result = myLectures.stream()
                .map(CompareTimetableDto::new)
                .collect(Collectors.toList());

        // 🔹 친구들 시간표 비교
        for (Long memberId : compareMemberDto.getMemberIds()) {
            Member member = em.find(Member.class, memberId);
            if (member == null) continue;

            // 친구의 대표 시간표 강의 조회
            List<Lecture> lectures = em.createQuery("""
            select l from Timetable t
            join t.timetableLectures tl
            join tl.lecture l
            where t.member.id = :memberId
            and t.isRepresent = true
            and t.year = :year
            and t.semester = :semester
        """, Lecture.class)
                    .setParameter("memberId", memberId)
                    .setParameter("year", year)
                    .setParameter("semester", semester)
                    .getResultList();

            // 겹치는 강의 처리
            for (Lecture lecture : lectures) {
                for (CompareTimetableDto dto : result) {
                    if (dto.getInternalLectureDto().getId().equals(lecture.getId())) {
                        dto.getUsernames().add(member.getUsername());
                        dto.getStudentIds().add(member.getStudentId());
                        break;
                    }
                }
            }
        }

        // 🔹 겹치는 강의만 필터링
        return result.stream()
                .filter(dto -> !dto.getStudentIds().isEmpty())
                .collect(Collectors.toList());
    }


    // 모든 강의 중복없이 가져오기
    public List<InternalLectureDto> compareFreeTime(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        // ✅ 중복 허용을 위해 Set 대신 List 사용
        List<Lecture> result = new ArrayList<>();

        compareMemberDto.getMemberIds().add(customUserDetails.getId());
        for (Long memberId : compareMemberDto.getMemberIds()) {

            List<Lecture> lectures = em.createQuery("""
            select l from Timetable t
            join t.timetableLectures tl
            join tl.lecture l
            where t.member.id = :memberId
            and t.isRepresent = true
            and t.year = :year
            and t.semester = :semester
        """, Lecture.class)
                    .setParameter("memberId", memberId)
                    .setParameter("year", year)
                    .setParameter("semester", semester)
                    .getResultList();

            result.addAll(
                lectures
            );
        }

        return result.stream()
                .map(InternalLectureDto::new)
                .toList();
    }

}
