package org.fiddich.api.domain.timetable;

import jakarta.persistence.EntityManager;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.api.domain.timetable.helper.TimeParser;
import org.fiddich.api.domain.timetable.helper.TimetableGenerator;
import org.fiddich.api.domain.timetable.helper.TimetableScorer;
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
import java.sql.Time;
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
        List<Lecture> lectures = lectureRepository.findAllByIds(createTimetableDto.getSelectedLectureIds());

        if(createTimetableDto.getIsRepresent()) {
            timetableRepository.clearMainTimetable(member.getId());
        }

        Timetable timetable = Timetable.builder()
                .member(member)
                .year(createTimetableDto.getYear())
                .semester(createTimetableDto.getSemester())
                .timeTableName(createTimetableDto.getTimeTableName())
                .isRepresent(createTimetableDto.getIsRepresent())
                .build();

        timetable.changeLectures(lectures);
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
                .map(InquiryTimeTableDto::new)
                .toList();
    }
    // 완료
    public InquiryTimeTableDto getMainTimetableWithLectures(String year, String semester) {
        // 학년도로 시간표 조회 메서드 사용
        List<InquiryTimeTableDto> findInquiryTimeTableDtos = getTimetablesAboutYearAndSemester(year, semester);
        // 만약 메인사간표가 없다면 null을 반환
        return findInquiryTimeTableDtos.stream().filter(t -> t.isRepresent()).findFirst().orElse(null);
    }
    // 완료
    public void editTimetable(Long timetableId, LectureIdsDto lectureIdsDto) {
        Timetable timetable = timetableRepository.findByIdWithTimetableLectures(timetableId)
                .orElseThrow(() -> new NoSuchElementException("시간표를 찾을 수 없습니다."));

        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIdsDto.getLectureIds());
        timetable.changeLectures(lectures);
    }
    // 완료
    public void deleteTimetable(Long timetableId) {
        lectureRepository.deleteCustomLectureByTimetable(timetableId);
        timetableRepository.deleteTimetable(timetableId);
    }

    // 완료
    public void changeMainTimetable(TimetableIdDto timetableIdDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = customUserDetails.getId();
        Long timetableId = timetableIdDto.getTimetableId();

        timetableRepository.clearMainTimetable(memberId);
        timetableRepository.updateMainTimetable(timetableId);
    }

    // 자동생성
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

    // 겹치는 강의에 회원정보 추가
    public List<CompareTimetableDto> compareTimetable(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = customUserDetails.getId();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        // 내 대표 시간표에서 강의 목록 조회 (강의로 바로 조회가능
        Timetable myMainTimetable = timetableRepository.findMainByMemberIdWithLectures(myId, year, semester).orElseThrow(() -> new NoSuchElementException("메인시간표가 없습니다."));
        List<Lecture> myLectures = myMainTimetable.getTimetableLectures().stream().map(TimetableLecture::getLecture).toList();

        // 🔹 결과 초기화
        List<CompareTimetableDto> compareTimetableDtos = myLectures.stream()
                .map(CompareTimetableDto::new)
                .toList();


        for(Long memberId : compareMemberDto.getMemberIds()) {
            Member friend = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));
            Timetable friendMainTimetable = timetableRepository.findMainByMemberIdWithLectures(friend.getId(), year, semester).orElse(null);

            if(friendMainTimetable != null) {
                List<Lecture> friendLectures = friendMainTimetable.getTimetableLectures().stream().map(TimetableLecture::getLecture).toList();

                for (Lecture friendLecture : friendLectures) {
                    for(CompareTimetableDto dto : compareTimetableDtos) {
                        if(dto.getInternalLectureDto().getId().equals(friendLecture.getId())) {
                            dto.getUsernames().add(friend.getUsername());
                            dto.getStudentIds().add(friend.getStudentId());
                            break;
                        }
                    }
                }

            }
        }

        return compareTimetableDtos;
    }


    // 모든 강의 중복없이 가져오기
    public List<InternalLectureDto> compareFreeTime(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        List<Lecture> result = new ArrayList<>();

        // 나도 포함
        compareMemberDto.getMemberIds().add(customUserDetails.getId());

        for (Long memberId : compareMemberDto.getMemberIds()) {

            Timetable timetable = timetableRepository.findMainByMemberIdWithLectures(memberId, year, semester).orElse(null);

            if (timetable != null) {
                List<Lecture> lectures = timetable.getTimetableLectures().stream().map(TimetableLecture::getLecture).toList();
                result.addAll(lectures);
            }
        }

        return result.stream()
                .map(InternalLectureDto::new)
                .toList();
    }

}
