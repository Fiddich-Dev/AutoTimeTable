package org.fiddich.api.domain.timetable;

import org.fiddich.api.domain.timetable.dto.CreateTimetableFilteringOptionDto;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableGenerator {

    private final List<Lecture> totalLectures;

    private int targetMajorCnt;
    private int targetCultureCnt;
    private List<Lecture> likeLecture;
    private List<Lecture> dislikeLecture;

    private int[][] usedTime = new int[7][1440];
    private List<List<Lecture>> makedTimeTable;

    public TimetableGenerator(List<Lecture> totalLectures) {
        this.totalLectures = totalLectures;
    }

    public void create(int targetMajorCnt, int targetCultureCnt, List<Lecture> likeLecture, List<Lecture> dislikeLecture, int includedMajorCnt, int includedCultureCnt, int[][] usedTime) {
        this.targetMajorCnt = targetMajorCnt;
        this.targetCultureCnt = targetCultureCnt;
        this.likeLecture = likeLecture;
        this.dislikeLecture = dislikeLecture;
        this.makedTimeTable = new ArrayList<>();
        this.usedTime = usedTime;

        List<Lecture> v = new ArrayList<>();
        // 포함될 강의 개수 추가
        LectureCnt lectureCnt = new LectureCnt(includedMajorCnt, includedCultureCnt);

        go(-1, v, lectureCnt);

        System.out.println(makedTimeTable.size());
    }



    void go(int start, List<Lecture> b, LectureCnt nowLectureCnt) {

        if(nowLectureCnt.major == targetMajorCnt && nowLectureCnt.culture == targetCultureCnt) {
            makedTimeTable.add(new ArrayList<>(b)); // ← 복사해서 추가
            return;
        }

        for(int i = start + 1; i < totalLectures.size(); i++) {

            // ❌ dislikeLecture에 포함된 강의는 건너뜀
            if (dislikeLecture.contains(totalLectures.get(i))) {
                continue;
            }

            if(canAddLectureAboutCode(b, totalLectures.get(i)) && canAddLectureAboutTime(totalLectures.get(i)) && canAddLectureAboutCnt(nowLectureCnt, totalLectures.get(i))) {
                b.add(totalLectures.get(i));
                fillUsedTime(totalLectures.get(i));
                addLectureCnt(totalLectures.get(i), nowLectureCnt);

                go(i, b, nowLectureCnt);

                b.remove(b.size() - 1);
                eraseUsedTime(totalLectures.get(i));
                removeLectureCnt(totalLectures.get(i), nowLectureCnt);
            }
        }
    }

    // 겹치는 학수번호 제외
    boolean canAddLectureAboutCode(List<Lecture> selectedLectures, Lecture targetLecture) {
        String targetCode = targetLecture.getCode();
        // 1. 이미 같은 코드가 존재하는지 확인
        boolean isDuplicateCode = selectedLectures.stream()
                .map(Lecture::getCode)
                .collect(Collectors.toSet())
                .contains(targetCode);

        if (isDuplicateCode) return false;
        return true;
    }


    // 겹치는 시간 제외
    boolean canAddLectureAboutTime(Lecture targetLecture) {
        // 2. 시간 겹침 확인
        String[] times = targetLecture.getTime().split(",");
        for (String time : times) {
            if(time.isBlank()) {
                continue;
            }
            int day = dayToInt(time.charAt(0));
            String[] startAndEnd = time.substring(1).split("-");

            int start = Integer.parseInt(startAndEnd[0]) % 100 + Integer.parseInt(startAndEnd[0]) / 100 * 60;
            int end = Integer.parseInt(startAndEnd[1]) % 100 + Integer.parseInt(startAndEnd[1]) / 100 * 60;

            for (int i = start; i < end; i++) {
                if (usedTime[day][i] == 1) return false; // 이미 사용 중인 시간
            }
        }

        return true;
    }

    // 강의 개수 조건 제외
    boolean canAddLectureAboutCnt(LectureCnt nowLectureCnt, Lecture targetLecture) {
        if(isCultureLecture(targetLecture)) {
            return nowLectureCnt.culture + 1 <= targetCultureCnt;
        }
        else {
            return nowLectureCnt.major + 1 <= targetMajorCnt;
        }
    }

    // 시간 칠하기
    void fillUsedTime(Lecture targetLecture) {
        String[] times = targetLecture.getTime().split(",");
        for(String time : times) {
            if(time.isBlank()) {
                continue;
            }
            int day = dayToInt(time.charAt(0));
            String[] startAndEnd = time.substring(1).split("-");
            int start = Integer.parseInt(startAndEnd[0]) % 100 + Integer.parseInt(startAndEnd[0]) / 100 * 60;
            int end = Integer.parseInt(startAndEnd[1]) % 100 + Integer.parseInt(startAndEnd[1]) / 100 * 60;

            for(int i = start; i < end; i++) {
                usedTime[day][i] = 1;
            }
        }
    }

    // 시간 지우기
    void eraseUsedTime(Lecture targetLecture) {
        String[] times = targetLecture.getTime().split(",");
        for(String time : times) {
            if(time.isBlank()) {
                continue;
            }
            int day = dayToInt(time.charAt(0));
            String[] startAndEnd = time.substring(1).split("-");
            int start = Integer.parseInt(startAndEnd[0]) % 100 + Integer.parseInt(startAndEnd[0]) / 100 * 60;
            int end = Integer.parseInt(startAndEnd[1]) % 100 + Integer.parseInt(startAndEnd[1]) / 100 * 60;

            for(int i = start; i < end; i++) {
                usedTime[day][i] = 0;
            }
        }
    }

    // 강의 개수 추가
    void addLectureCnt(Lecture targetLecture, LectureCnt p) {
        if(isCultureLecture(targetLecture)) {
            p.culture++;
        }
        else {
            p.major++;
        }
    }
    // 강의 개수 제거
    void removeLectureCnt(Lecture targetLecture, LectureCnt p) {
        if(isCultureLecture(targetLecture)) {
            p.culture--;
        }
        else {
            p.major--;
        }
    }


    // 문자를 숫자로
    static int dayToInt(char day) {
        switch (day) {
            case '월' : return 0;
            case '화' : return 1;
            case '수' : return 2;
            case '목' : return 3;
            case '금' : return 4;
            case '토' : return 5;
            case '일' : return 6;
            default: throw new IllegalArgumentException("잘못된 요일 문자입니다: " + day);
        }
    }

    private boolean isCultureLecture(Lecture lecture) {
        if(lecture.getCategory().getParent().getName().equals("교양/기타")) {
            return true;
        }
        return false;
    }

    static class LectureCnt {
        int major;
        int culture;

        public LectureCnt(int major, int culture) {
            this.major = major;
            this.culture = culture;
        }
    }


    public List<List<Lecture>> getMakedTimeTable(int minCredit, int maxCredit) {
        return makedTimeTable.stream().filter(t -> {
            int credit = getTotalCredit(t);
            return credit >= minCredit && credit <= maxCredit;
        }).toList();
    }


    // 학점 계산
    int getTotalCredit(List<Lecture> lectures) {
        int sum = 0;
        for(Lecture lecture : lectures) {
            sum += Integer.parseInt(lecture.getCredit());
        }
        return sum;
    }

    // 오전 수업 비율
    double getMorningRatio(List<Lecture> lectures) {
        int morningMinutes = 0;
        int totalMinutes = 0;
        for (Lecture l : lectures) {
            for (String time : l.getTime().split(",")) {
                if(time.isBlank()) continue;
                String[] parts = time.substring(1).split("-");
                int start = Integer.parseInt(parts[0]) / 100;
                int end = Integer.parseInt(parts[1]) / 100;
                if (start < 12) morningMinutes += (end - start) * 60;
                totalMinutes += (end - start) * 60;
            }
        }
        return totalMinutes == 0 ? 0 : (double) morningMinutes / totalMinutes;
    }

    // 요일 수
    int getSchoolDays(List<Lecture> lectures) {
        return (int) lectures.stream()
                .flatMap(l -> List.of(l.getTime().split(",")).stream())
                .map(s -> s.charAt(0))
                .distinct()
                .count();
    }

    // 1시간 이상의 공강의 수
    public int countEmptyGapsOverOneHour(List<Lecture> lectures) {
        // 각 요일별 강의 시간을 나눔
        Map<Integer, List<int[]>> timeByDay = new HashMap<>();

        for (Lecture lecture : lectures) {
            String[] times = lecture.getTime().split(",");
            for (String time : times) {
                if (time.isBlank()) continue;

                int day = dayToInt(time.charAt(0));
                String[] startEnd = time.substring(1).split("-");

                int start = parseTimeToMinutes(startEnd[0]);
                int end = parseTimeToMinutes(startEnd[1]);

                timeByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(new int[]{start, end});
            }
        }

        int totalGaps = 0;

        for (List<int[]> intervals : timeByDay.values()) {
            // 정렬
            intervals.sort(Comparator.comparingInt(a -> a[0]));

            for (int i = 1; i < intervals.size(); i++) {
                int prevEnd = intervals.get(i - 1)[1];
                int currStart = intervals.get(i)[0];

                if (currStart - prevEnd >= 60) {
                    totalGaps++;
                }
            }
        }

        return totalGaps;
    }

    private int parseTimeToMinutes(String time) {
        int hour = Integer.parseInt(time) / 100;
        int minute = Integer.parseInt(time) % 100;
        return hour * 60 + minute;
    }

    public List<List<Lecture>> getFilteredTimeTables(CreateTimetableFilteringOptionDto option) {
        return makedTimeTable.stream()
                .filter(t -> {
                    int credit = getTotalCredit(t);
                    return credit >= option.getMinCredit() && credit <= option.getMaxCredit();
                })
                .sorted((a, b) -> {
                    // 조건에 따라 정렬 기준을 바꾸자
                    if (option.isPreferMorning()) {
                        return Double.compare(getMorningRatio(b), getMorningRatio(a)); // 오전이 더 많은 쪽이 앞으로
                    } else if (option.isPreferAfternoon()) {
                        return Double.compare(getMorningRatio(a), getMorningRatio(b)); // 오후가 더 많은 쪽이 앞으로
                    } else if (option.isMinimizeSchoolDays()) {
                        return Integer.compare(getSchoolDays(a), getSchoolDays(b)); // 요일 수가 적은 게 먼저
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }


}
