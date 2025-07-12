package com.fiddich.LectureMapping;

import com.fiddich.LectureMapping.entity.Lecture;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LectureParser {

    public static String timeParse(String input) {
        String[] lines = input.split("<br>");
        StringBuilder result = new StringBuilder();

        Pattern pattern = Pattern.compile("([월화수목금토일])([0-9]{1,2}):([0-9]{2})-([0-9]{1,2}):([0-9]{2})");

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                String day = matcher.group(1);
                int startHour = Integer.parseInt(matcher.group(2));
                int startMin = Integer.parseInt(matcher.group(3));
                int endHour = Integer.parseInt(matcher.group(4));
                int endMin = Integer.parseInt(matcher.group(5));

                int startTime = startHour * 100 + startMin;
                int endTime = endHour * 100 + endMin;

                result.append(day)
                        .append(startTime)
                        .append("-")
                        .append(endTime);

                if (i < lines.length - 1) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    public static Lecture parseLecture(String input) {
        Lecture lecture = new Lecture();

        String codeSection = extractValue(input, "code");
        int idx = codeSection.lastIndexOf('-');
        String code = codeSection.substring(0, idx);

        String time = extractValue(input, "time");


        lecture.setCode(code);
        lecture.setCodeSection(extractValue(input, "code"));
        lecture.setName(extractValue(input, "name"));
        lecture.setProfessor(extractValue(input, "professor")) ;
        lecture.setType(extractValue(input, "type"));
        lecture.setTime(timeParse(time));
        lecture.setPlace(extractValue(input, "place"));
        lecture.setCredit(extractValue(input, "credit"));
        lecture.setTarget(extractValue(input, "target"));
        lecture.setNotice(extractValue(input, "notice"));

        return lecture;
    }

// 금10:00-10:50【61703】<br>금11:00-11:50【61703】【1h(ON)+2h(OFF)】
// <br>로 나누면
// 금10:00-10:50【61703】
// 금11:00-11:50【61703】【1h(ON)+2h(OFF)】

// 특수문자 제거
// 금10:00-10:50
// 금11:00-11:50

// 타입변경
// 금600-650, 금660-710

    private static String extractValue(String input, String key) {
        String pattern = key + "=\"";
        int startIndex = input.indexOf(pattern);
        if (startIndex == -1) return null;

        startIndex += pattern.length();
        int endIndex = input.indexOf("\"", startIndex);
        if (endIndex == -1) return null;

        return input.substring(startIndex, endIndex);
    }
}

