package com.fiddich.LectureMapping;

public class LectureParser {

    public static CultureLecture parseLecture(String input) {
        CultureLecture cultureLecture = new CultureLecture();

        cultureLecture.setCode(extractValue(input, "code"));
        cultureLecture.setName(extractValue(input, "name"));
        cultureLecture.setProfessor(extractValue(input, "professor")) ;
        cultureLecture.setType(extractValue(input, "type"));
        cultureLecture.setTime(extractValue(input, "time"));
        cultureLecture.setPlace(extractValue(input, "place"));
        cultureLecture.setCredit(extractValue(input, "credit"));
        cultureLecture.setTarget(extractValue(input, "target"));
        cultureLecture.setNotice(extractValue(input, "notice"));

        return cultureLecture;
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

