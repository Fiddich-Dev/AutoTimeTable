package org.fiddich.api.domain.timetable;


import jakarta.persistence.EntityManager;
import org.fiddich.api.domain.everytime.EverytimeRequester;
import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.api.domain.timetable.dto.*;
import org.fiddich.api.domain.timetable.dto.request.CompareMemberDto;
import org.fiddich.api.domain.timetable.dto.request.CreateTimetableOptionDto;
import org.fiddich.api.domain.timetable.dto.request.EditTimetableDto;
import org.fiddich.api.domain.timetable.dto.request.TimetableIdDto;
import org.fiddich.api.domain.timetable.dto.response.CompareTimetableDto;
import org.fiddich.api.domain.timetable.dto.response.YearAndSemesterDto;
import org.fiddich.api.domain.timetable.helper.TimetableGenerator;
import org.fiddich.api.domain.timetable.helper.TimetableScorer;
import org.fiddich.coreinfradomain.domain.Lecture.CustomLecture;
import org.fiddich.coreinfradomain.domain.Lecture.OfficialLecture;
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

        List<OfficialLecture> officialLectures = createTimetableDto.getLectures()
                .stream().filter(l -> !l.getCodeSection().isEmpty())
                .map(l -> OfficialLecture.builder().codeSection(l.getCodeSection()).build())
                .collect(Collectors.toList());

        List<CustomLecture> customLectures = createTimetableDto.getLectures()
                .stream().filter(l -> l.getCodeSection().isEmpty())
                .map(l -> CustomLecture
                        .builder()
                        .codeSection(l.getCodeSection())
                        .name(l.getName())
                        .professor(l.getProfessor())
                        .type(l.getType())
                        .credit(l.getCredit())
                        .notice(l.getNotice())
                        .lectureTimes(l.decodeTime())
                        .build())
                .collect(Collectors.toList());

        for(CustomLecture customLecture : customLectures) {
            customLecture.addLectureTime(customLecture.getLectureTimes());
        }

        if(createTimetableDto.isRepresent()) {
            timetableRepository.clearMainTimetable(member.getId());
        }

        Timetable timetable = Timetable.builder()
                .member(member)
                .year(createTimetableDto.getYear())
                .semester(createTimetableDto.getSemester())
                .timeTableName(createTimetableDto.getTimeTableName())
                .isRepresent(createTimetableDto.isRepresent())
                .build();


        timetable.changeLectures(officialLectures, customLectures);
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
        List<Timetable> timetables = timetableRepository.findTimetablesWithLecturesByMemberId(customUserDetails.getId(), year, semester);

        return timetables.stream()
//                .filter(t -> t.getYear().equals(year) && t.getSemester().equals(semester))
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

    // 커스텀은 그냥 저장
    // 공식은 codeSection을 받아서 에타에 조회해서 저장
    public void editTimetable(Long timetableId, List<InternalLectureDto> internalLectureDtos) {
        Timetable timetable = timetableRepository.findByIdWithTimetableLectures(timetableId)
                .orElseThrow(() -> new NoSuchElementException("시간표를 찾을 수 없습니다."));

        List<OfficialLecture> officialLectures = internalLectureDtos.stream()
                .filter(l -> !l.getType().isEmpty())
                .map(InternalLectureDto::getCodeSection)
                .map(cs -> OfficialLecture.builder().codeSection(cs).build())
                .collect(Collectors.toList());


        List<CustomLecture> customLectures = internalLectureDtos.stream()
                .filter(l -> l.getType().isEmpty())
                .map(l -> CustomLecture
                        .builder()
                        .codeSection(l.getCodeSection())
                        .name(l.getName())
                        .professor(l.getProfessor())
                        .type(l.getType())
                        .place("")
                        .credit(l.getCredit())
                        .target("")
                        .notice(l.getNotice())
                        .lectureTimes(l.decodeTime())
                        .build())
                .collect(Collectors.toList());

        for(CustomLecture customLecture : customLectures) {
            customLecture.addLectureTime(customLecture.getLectureTimes());
        }

        timetable.changeLectures(officialLectures, customLectures);
    }

    // 완료
    public void deleteTimetable(Long timetableId) {
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

        List<String> likeLectureCodes = optionDto.getLikeOfficialLectureCodeSection();
        List<String> dislikeLectureCodes = optionDto.getDislikeOfficialLectureCodeSection();

        List<String> categoryIds = optionDto.getCategoryIds();

        int[][] usedTime = optionDto.getUsedTime();

        // 고른 전공들의 강의 찾기
        List<Subject> subjects = new ArrayList<>();
        for(String categoryId : categoryIds) {
            try {
                subjects.addAll(EverytimeRequester.findSubjectsByCategoryId(categoryId, optionDto.getYear(), optionDto.getSemester()));
            } catch (IOException e) {
                System.out.println("에타 api 조회 못함");
            }
        }

        // 포함시킬 강의들
        List<Subject> likeSubjects = new ArrayList<>();
        for(String likeLectureCode : likeLectureCodes) {
            String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", likeLectureCode);
            try {
                likeSubjects.add(EverytimeRequester.fetchSearchedLectures(keywordJson, optionDto.getYear(), optionDto.getSemester(), 50, 0).get(0));
            } catch (IOException e) {
                System.out.println("에타 api 조회 못함");
            }
        }

        // 제외시킬 강의들
        List<Subject> dislikeSubjects = new ArrayList<>();
        for(String dislikeLectureCode : dislikeLectureCodes) {
            String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", dislikeLectureCode);
            try {
                dislikeSubjects.add(EverytimeRequester.fetchSearchedLectures(keywordJson, optionDto.getYear(), optionDto.getSemester(), 50, 0).get(0));
            } catch (IOException e) {
                System.out.println("에타 api 조회 못함");
            }
        }

        int includedMajorCnt = 0;
        int includedCultureCnt = 0;

        // 포함시킬 강의중에 교양이 몇개인지 찾아야함
        for(Subject subject : likeSubjects) {
            if(subject.getType().equals("교양")) {
                includedCultureCnt++;
            }
            else {
                includedMajorCnt++;
            }
        }

        TimetableGenerator timetableGenerator = new TimetableGenerator(subjects);
        timetableGenerator.create(targetMajorCnt, targetCultureCnt, likeSubjects, dislikeSubjects, includedMajorCnt, includedCultureCnt, usedTime);

        TimetableScorer scorer = new TimetableScorer(optionDto.isPreferMorning(), optionDto.isPreferAfternoon());

        List<List<Subject>> sortedTimetable = timetableGenerator.getMakedTimeTable(optionDto.getMinCredit(), optionDto.getMaxCredit()).stream()
                .sorted(Comparator.comparingInt(scorer::score).reversed()) // 점수 높은 게 좋은 시간표
                .toList();

        return sortedTimetable.stream()
                .map(innerList ->
                        innerList.stream()
                                .map(InternalLectureDto::new) // 각 Lecture → InternalLectureDto로 변환
                                .collect(Collectors.toList())
                )
                .collect(Collectors.toList());
    }

    // 겹치는 강의에 회원정보 추가
    // 커스텀은 겹칠수가 없음
    public List<CompareTimetableDto> compareTimetable(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = customUserDetails.getId();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        // 내 메인 시간표에서 강의 목록 조회
        Timetable myMainTimetable = timetableRepository.findMainByMemberIdWithLectures(myId, year, semester)
                .orElseThrow(() -> new NoSuchElementException("메인시간표가 없습니다."));
        List<InternalLectureDto> myLectures = new ArrayList<>();

        // 공식강의 넣기
        List<String> officialLectures = myMainTimetable.getOfficialLectures().stream().map(ol -> ol.getCodeSection()).toList();
        for(String officialLecture : officialLectures) {
            String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", officialLecture);
            try {
                Subject findSubject = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, 50, 0).get(0);
                myLectures.add(new InternalLectureDto(findSubject));
            } catch (IOException e) {
                System.out.println("에타 api 조회 못함");
            }
        }

        // 결과 저장용 Map: 강의 ID → CompareTimetableDto
        Map<String, CompareTimetableDto> lectureDtoMap = new HashMap<>();

        for (Long memberId : compareMemberDto.getMemberIds()) {
            Member friend = memberRepository.findById(memberId)
                    .orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));
            Timetable friendMainTimetable = timetableRepository.findMainByMemberIdWithLectures(friend.getId(), year, semester)
                    .orElse(null);

            if (friendMainTimetable == null) continue;

            List<InternalLectureDto> friendLectures = new ArrayList<>();

            // 공식강의 넣기
            List<String> friendOfficialLectures = friendMainTimetable.getOfficialLectures().stream().map(ol -> ol.getCodeSection()).toList();
            for(String friendOfficialLecture : friendOfficialLectures) {
                String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", friendOfficialLecture);
                try {
                    Subject findSubject = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, 50, 0).get(0);
                    friendLectures.add(new InternalLectureDto(findSubject));
                } catch (IOException e) {
                    System.out.println("에타 api 조회 못함");
                }
            }

            for (InternalLectureDto friendLecture : friendLectures) {
                String lectureCodeSection = friendLecture.getCodeSection();

                // 내 시간표에 포함된 강의인지 확인
                boolean isOverlapping = myLectures.stream()
                        .anyMatch(myLecture -> myLecture.getCodeSection().equals(lectureCodeSection));

                if (!isOverlapping) continue;

                // 이미 있는 DTO를 가져오거나 새로 생성
                CompareTimetableDto dto;
                if (lectureDtoMap.containsKey(lectureCodeSection)) {
                    dto = lectureDtoMap.get(lectureCodeSection);
                } else {
                    dto = new CompareTimetableDto();
                    dto.setInternalLectureDto(friendLecture);
                    lectureDtoMap.put(lectureCodeSection, dto);
                }

                // 중복 없이 정보 추가
                if (!dto.getUsernames().contains(friend.getUsername())) {
                    dto.getUsernames().add(friend.getUsername());
                }
                if (!dto.getStudentIds().contains(friend.getStudentId())) {
                    dto.getStudentIds().add(friend.getStudentId());
                }
            }
        }

        return new ArrayList<>(lectureDtoMap.values());
    }


    // 모든 강의 중복있게 가져오기
    // 커스텀강의의 시간이 중복으로 가져와짐
    public List<InternalLectureDto> compareFreeTime(CompareMemberDto compareMemberDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String year = compareMemberDto.getYear();
        String semester = compareMemberDto.getSemester();

        List<InternalLectureDto> result = new ArrayList<>();

        // 나도 포함
        compareMemberDto.getMemberIds().add(customUserDetails.getId());

        for (Long memberId : compareMemberDto.getMemberIds()) {

            Timetable timetable = timetableRepository.findMainByMemberIdWithLectures(memberId, year, semester).orElse(null);

            System.out.println(timetable.getCustomLectures().size());

            if (timetable != null) {
                // 공식강의 넣기
                List<String> officialLectures = timetable.getOfficialLectures().stream().map(ol -> ol.getCodeSection()).toList();
                for(String officialLecture : officialLectures) {
                    String keywordJson = String.format("{\"type\":\"%s\",\"keyword\":\"%s\"}", "code", officialLecture);
                    try {
                        Subject findSubject = EverytimeRequester.fetchSearchedLectures(keywordJson, year, semester, 50, 0).get(0);
                        result.add(new InternalLectureDto(findSubject));
                    } catch (IOException e) {
                        System.out.println("에타 api 조회 못함");
                    }
                }

                // 커스텀 강의 넣기
                result.addAll(timetable.getCustomLectures().stream().map(cl -> new InternalLectureDto(cl)).toList());
            }
        }

        return result;
    }

}
