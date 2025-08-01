package org.fiddich.api.domain.timetable.helper;

import org.fiddich.api.domain.everytime.dto.Subject;
import org.fiddich.api.domain.everytime.dto.TimePlace;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;

import java.sql.Time;
import java.util.*;

public class TimetableScorer {

    private final boolean preferMorning;
    private final boolean preferAfternoon;

    public TimetableScorer(boolean preferMorning, boolean preferAfternoon) {
        this.preferMorning = preferMorning;
        this.preferAfternoon = preferAfternoon;
    }

    public int score(List<Subject> subjects) {
        int score = 0;

        // 1. 빈 시간(공강) 개수 적을수록 좋음
        int emptyGaps = countEmptyGapsOverOneHour(subjects);
        score -= emptyGaps * 10;

        // 2. 수업 있는 요일 개수 적을수록 좋음
        int days = countDaysWithLectures(subjects);
        score -= days * 10;

        // 3. 오전 수업 선호
        if (preferMorning) {
            int morningCount = countMorningLectures(subjects);
            score += morningCount * 5;  // 많을수록 좋음
        }

        // 4. 오후 수업 선호
        if (preferAfternoon) {
            int afternoonCount = countAfternoonLectures(subjects);
            score += afternoonCount * 5;  // 많을수록 좋음
        }

        return score;
    }

    // 오전 수업: 12시 이전 시작
    private int countMorningLectures(List<Subject> subjects) {
        return (int) subjects.stream()
                .flatMap(s -> s.getTimeplaceList().stream())
                .map(tp -> Integer.parseInt(tp.getStart()))
                .filter(start -> start * 5 < 12 * 60)
                .count();
    }

    // 오후 수업: 13시 이후 시작
    private int countAfternoonLectures(List<Subject> subjects) {
        return (int) subjects.stream()
                .flatMap(s -> s.getTimeplaceList().stream())
                .map(tp -> Integer.parseInt(tp.getStart()))
                .filter(start -> start * 5 >= 13 * 60)
                .count();
    }

    // 요일 개수
    private int countDaysWithLectures(List<Subject> subjects) {
        return (int) subjects.stream()
                .flatMap(s -> s.getTimeplaceList().stream())
                .map(tp -> Integer.parseInt(tp.getDay()))
                .distinct()
                .count();
    }

    // 공강 개수 (1시간 이상 빈 시간)
    private int countEmptyGapsOverOneHour(List<Subject> subjects) {
        Map<Integer, List<int[]>> timeByDay = new HashMap<>();

        for (Subject subject : subjects) {
            List<TimePlace> timePlaceList = subject.getTimeplaceList();
            for (TimePlace timePlace : timePlaceList) {
                if(timePlace == null) {
                    continue;
                }
                int day = Integer.parseInt(timePlace.getDay());
                int start = Integer.parseInt(timePlace.getStart()) * 5;
                int end = Integer.parseInt(timePlace.getEnd()) * 5;

                timeByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(new int[]{start, end});
            }
        }

        int totalGaps = 0;
        for (List<int[]> intervals : timeByDay.values()) {
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

}

