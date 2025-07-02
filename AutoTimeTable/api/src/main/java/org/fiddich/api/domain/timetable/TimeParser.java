package org.fiddich.api.domain.timetable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

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

}
