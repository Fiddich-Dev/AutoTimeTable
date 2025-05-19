package com.fiddich.LectureMapping;

public class LectureParser {

    public static Lecture parseLecture(String input) {
        Lecture lecture = new Lecture();

        lecture.setCode(extractValue(input, "code"));
        lecture.setName(extractValue(input, "name"));
        lecture.setProfessor(extractValue(input, "professor")) ;
        lecture.setType(extractValue(input, "type"));
        lecture.setTime(extractValue(input, "time"));
        lecture.setPlace(extractValue(input, "place"));
        lecture.setCredit(extractValue(input, "credit"));
        lecture.setTarget(extractValue(input, "target"));
        lecture.setNotice(extractValue(input, "notice"));

        return lecture;
    }

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

