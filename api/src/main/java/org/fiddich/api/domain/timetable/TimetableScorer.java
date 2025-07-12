package org.fiddich.api.domain.timetable;

import org.fiddich.coreinfradomain.domain.Lecture.Lecture;

import java.util.*;

public class TimetableScorer {

    private final boolean preferMorning;
    private final boolean preferAfternoon;

    public TimetableScorer(boolean preferMorning, boolean preferAfternoon) {
        this.preferMorning = preferMorning;
        this.preferAfternoon = preferAfternoon;
    }

    public int score(List<Lecture> lectures) {
        int score = 0;

        // 1. 빈 시간(공강) 개수 적을수록 좋음
        int emptyGaps = countEmptyGapsOverOneHour(lectures);
        score -= emptyGaps * 10;

        // 2. 수업 있는 요일 개수 적을수록 좋음
        int days = countDaysWithLectures(lectures);
        score -= days * 10;

        // 3. 오전 수업 선호
        if (preferMorning) {
            int morningCount = countMorningLectures(lectures);
            score += morningCount * 5;  // 많을수록 좋음
        }

        // 4. 오후 수업 선호
        if (preferAfternoon) {
            int afternoonCount = countAfternoonLectures(lectures);
            score += afternoonCount * 5;  // 많을수록 좋음
        }

        return score;
    }

    // 오전 수업: 12시 이전 시작
    private int countMorningLectures(List<Lecture> lectures) {
        return (int) lectures.stream()
                .flatMap(l -> Arrays.stream(l.getTime().split(",")))
                .map(String::trim)
                .filter(time -> !time.isEmpty())
                .mapToInt(t -> parseTimeToMinutes(t.substring(1).split("-")[0]))
                .filter(start -> start < 12 * 60)
                .count();
    }

    // 오후 수업: 15시 이후 시작
    private int countAfternoonLectures(List<Lecture> lectures) {
        return (int) lectures.stream()
                .flatMap(l -> Arrays.stream(l.getTime().split(",")))
                .map(String::trim)
                .filter(time -> !time.isEmpty())
                .mapToInt(t -> parseTimeToMinutes(t.substring(1).split("-")[0]))
                .filter(start -> start >= 15 * 60)
                .count();
    }

    // 요일 개수
    private int countDaysWithLectures(List<Lecture> lectures) {
        return (int) lectures.stream()
                .flatMap(l -> Arrays.stream(l.getTime().split(",")))
                .map(String::trim)
                .filter(time -> !time.isEmpty())
                .map(t -> TimetableGenerator.dayToInt(t.charAt(0)))
                .distinct()
                .count();
    }

    // 공강 개수 (1시간 이상 빈 시간)
    private int countEmptyGapsOverOneHour(List<Lecture> lectures) {
        Map<Integer, List<int[]>> timeByDay = new HashMap<>();

        for (Lecture lecture : lectures) {
            String[] times = lecture.getTime().split(",");
            for (String time : times) {
                if (time.isBlank()) continue;

                int day = TimetableGenerator.dayToInt(time.charAt(0));
                String[] startEnd = time.substring(1).split("-");
                int start = parseTimeToMinutes(startEnd[0]);
                int end = parseTimeToMinutes(startEnd[1]);

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

    private int parseTimeToMinutes(String timeStr) {
        int hour = Integer.parseInt(timeStr) / 100;
        int minute = Integer.parseInt(timeStr) % 100;
        return hour * 60 + minute;
    }
}

