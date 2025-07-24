package org.fiddich.api.domain.timetable.helper;

import org.fiddich.coreinfradomain.domain.Lecture.LectureTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

//    public static String timeParse(String input) {
//        String[] lines = input.split("<br>");
//        StringBuilder result = new StringBuilder();
//
//        Pattern pattern = Pattern.compile("([월화수목금토일])([0-9]{1,2}):([0-9]{2})-([0-9]{1,2}):([0-9]{2})");
//
//        for (int i = 0; i < lines.length; i++) {
//            Matcher matcher = pattern.matcher(lines[i]);
//            if (matcher.find()) {
//                String day = matcher.group(1);
//                int startHour = Integer.parseInt(matcher.group(2));
//                int startMin = Integer.parseInt(matcher.group(3));
//                int endHour = Integer.parseInt(matcher.group(4));
//                int endMin = Integer.parseInt(matcher.group(5));
//
//                int startTime = startHour * 100 + startMin;
//                int endTime = endHour * 100 + endMin;
//
//                result.append(day)
//                        .append(startTime)
//                        .append("-")
//                        .append(endTime);
//
//                if (i < lines.length - 1) {
//                    result.append(",");
//                }
//            }
//        }
//        return result.toString();
//    }

    public static String timeParse(String day, String start, String end) {
        StringBuilder time = new StringBuilder();
        time.append(numToDay(day)).append(numToTime(start)).append("-").append(numToTime(end));
        return time.toString();
    }

    // input : 월1200-1315
    public static LectureTime timeParse(String dayAndTime) {
        String day = dayAndTime.substring(0, 1);
        String[] times = dayAndTime.substring(1).split("-");
        String start = times[0];
        String end = times[1];
        System.out.println(day + " : " + start + " : " + end);
        System.out.println(dayToNum(day));
        System.out.println(timeToNum(start));
        System.out.println(timeToNum(end));
        return new LectureTime(dayToNum(day), timeToNum(start), timeToNum(end));
    }


    private static String numToDay(String num) {
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

    private static String numToTime(String time) {
        int num = Integer.parseInt(time);
        int hour = num * 5 / 60;
        int minute = num * 5 % 60;
        return String.format("%d%02d", hour, minute);
    }

    private static int dayToNum(String day) {
        return switch (day) {
            case "월" -> 0;
            case "화" -> 1;
            case "수" -> 2;
            case "목" -> 3;
            case "금" -> 4;
            case "토" -> 5;
            case "일" -> 6;
            default -> throw new IllegalArgumentException("잘못된 요일: " + day);
        };
    }

    // input : 1315
    private static int timeToNum(String onlyTime) {
        int hour = Integer.parseInt(onlyTime) / 100;
        int minute = Integer.parseInt(onlyTime) % 100;
        return (hour * 60 + minute) / 5;
    }

    private static class StartAndEndTime {
        int start;
        int end;

        public StartAndEndTime(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }



}
