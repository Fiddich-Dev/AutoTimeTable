package org.fiddich.api.domain.timetable.helper;

import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.timetable.Category;
import org.fiddich.api.domain.timetable.Subject;
import org.fiddich.api.domain.timetable.TimePlace;
import org.fiddich.api.domain.timetable.dto.CreateTimetableWithExternalLecturesDto;
import org.fiddich.api.domain.timetable.dto.ExternalLectureDto;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

@Slf4j
public class EverytimeRequester {

    final static String campusId = "13";

    // 모든 학과 가져오기 (상위학과 제외)
    public static List<Category> findAllCategories(String year, String semester) throws IOException {

        Document doc =  Jsoup.connect("https://api.everytime.kr/find/timetable/subject/filter/list")
                .method(Connection.Method.POST)
                .headers(getCommonHeaders())
                .cookies(getCommonCookies())
                .data("year", year)
                .data("semester", semester)
                .post();

        Element campus = doc.selectFirst("campus");

        Elements categoryElements = campus.select("categories > category");

        List<Category> categories = new ArrayList<>();

        for (Element category : categoryElements) {
            String id = category.attr("id");
            String name = category.attr("name");
            String order = category.attr("order");
            String parentId = category.hasAttr("parentId") ? category.attr("parentId") : null;

            if(parentId == null) {
                continue;
            }

            categories.add(new Category(id, name, order, parentId));
        }

        return categories;
    }

    // 학과번호로 페이징된 강의 찾기
    public static List<Subject> findSubjectsByCategoryId(String categoryId, String year, String semester) throws IOException {
        List<Subject> allSubjects = new ArrayList<>();

        int start = 0;
        int size = 50;

        while(true) {
            List<Subject> findSubjects = fetchLecturesByCategory(categoryId, year, semester, size, start);
            if(findSubjects.isEmpty()) {
                break;
            }
            allSubjects.addAll(findSubjects);
            start += size;
        }
        return allSubjects;
    }

    private static List<Subject> fetchLecturesByCategory(String categoryId,
                                        String year,
                                        String semester,
                                        int limit,
                                        int start) throws IOException {
        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/subject/list")
                .method(Connection.Method.POST)
                .headers(getCommonHeaders())
                .cookies(getCommonCookies())
                .data("campusId", campusId)
                .data("year", year)
                .data("semester", semester)
                .data("limitNum", String.valueOf(limit))
                .data("startNum", String.valueOf(start))
                .data("categoryId", categoryId)
                .post();

        List<Subject> subjectList = new ArrayList<>();

        Elements subjects = doc.select("subject");

        for (Element el : subjects) {
            List<TimePlace> timeplaceList = new ArrayList<>();
            Elements timeplaces = el.select("timeplace");

            for (Element tp : timeplaces) {
                TimePlace timePlace = new TimePlace(
                        tp.attr("day"),
                        tp.attr("start"),
                        tp.attr("end"),
                        tp.attr("place")
                );
                timeplaceList.add(timePlace);
            }

            Subject subject = new Subject(
                    el.attr("id"),
                    el.attr("code"),
                    el.attr("name"),
                    el.attr("professor"),
                    el.attr("type"),
                    el.attr("time"),
                    el.attr("place"),
                    el.attr("credit"),
                    el.attr("capacity"),
                    el.attr("popular"),
                    el.attr("target"),
                    el.attr("notice"),
                    el.attr("lectureId"),
                    el.attr("lectureRate"),
                    timeplaceList
            );
            subjectList.add(subject);
        }
        return subjectList;
    }

    // 키워드로 강의 검색하기
    // code, name, professor
    public static List<Subject> fetchSearchedLectures(String keyword,
                                                String year,
                                                String semester,
                                                int limit,
                                                int start) throws IOException {

        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/subject/list")
                .method(Connection.Method.POST)
                .headers(getCommonHeaders())
                .cookies(getCommonCookies())
                .data("campusId", campusId)
                .data("year", year)
                .data("semester", semester)
                .data("limitNum", String.valueOf(limit))
                .data("startNum", String.valueOf(start))
                .data("keyword", keyword)
                .post();

        List<Subject> subjectList = new ArrayList<>();
        Elements subjects = doc.select("subject");

        for (Element el : subjects) {
            List<TimePlace> timeplaceList = new ArrayList<>();
            Elements timeplaces = el.select("timeplace");

            for (Element tp : timeplaces) {
                TimePlace timePlace = new TimePlace(
                        tp.attr("day"),
                        tp.attr("start"),
                        tp.attr("end"),
                        tp.attr("place")
                );
                timeplaceList.add(timePlace);
            }

            Subject subject = new Subject(
                    el.attr("id"),
                    el.attr("code"),
                    el.attr("name"),
                    el.attr("professor"),
                    el.attr("type"),
                    el.attr("time"),
                    el.attr("place"),
                    el.attr("credit"),
                    el.attr("capacity"),
                    el.attr("popular"),
                    el.attr("target"),
                    el.attr("notice"),
                    el.attr("lectureId"),
                    el.attr("lectureRate"),
                    timeplaceList
            );
            subjectList.add(subject);
        }
        return subjectList;
    }


    // 쿠키 설정
    private static Map<String, String> getCommonCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("_ga", "GA1.1.1955426778.1751110516");
        cookies.put("x-et-device", "9970041");
        cookies.put("etsid", "s%3ACueX3hGHlZ5SwbOQB5dJtbaNtrJNLXc-.iK%2FK1fxt25gcHSLBHXLv0SKiA25590hFld4mOmECkmw");
        cookies.put("_ga_85ZNEFVRGL", "GS2.1.s1751268570$o4$g1$t1751269458$j60$l0$h0");
        return cookies;
    }

    // 헤더 설정
    private static Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-encoding", "gzip, deflate, br, zstd");
        headers.put("accept-language", "ko,en-US;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6");
        headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("origin", "https://everytime.kr");
        headers.put("referer", "https://everytime.kr/");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"macOS\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-site");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        return headers;
    }

    // 특정 identifier로 시간표 조회
    private static List<ExternalLectureDto> findByEveryTimetableLectureById(String identifier) throws IOException {

        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/table/friend")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://everytime.kr/")
                .data("identifier", identifier)
                .data("friendInfo", "true")
                .timeout(5000)
                .post();

        Element tableElement = doc.selectFirst("table");
        Elements subjects = tableElement.select("subject");

        List<ExternalLectureDto> externalLectureDtos = new ArrayList<>();

        for (Element subject : subjects) {
            ExternalLectureDto externalLectureDto = new ExternalLectureDto();

            String subjectId = subject.attr("id");
            String fullCode = subject.selectFirst("internal").attr("value"); // codeSection
            String codePrefix = fullCode.split("-")[0]; // code

            Elements rawTimes = subject.selectFirst("time").select("data");
            StringBuilder time = new StringBuilder();
            for(Element rawTime : rawTimes) {
                String day = rawTime.attr("day");
                String start = rawTime.attr("starttime");
                String end = rawTime.attr("endtime");
                time.append(TimeParser.timeParse(day, start, end)).append(",");
            }
            if (!time.isEmpty()) {
                time.setLength(time.length() - 1); // 마지막 문자 제거 (예: ',' 제거)
            }
            String finalTime = time.toString(); // String으로 변환

            String professor = subject.selectFirst("professor").attr("value");
            String name = subject.selectFirst("name").attr("value");
            String credit = subject.selectFirst("credit").attr("value");

            externalLectureDto.setSubjectId(subjectId);
            externalLectureDto.setCode(codePrefix);
            externalLectureDto.setCodeSection(fullCode);
            externalLectureDto.setProfessor(professor);
            externalLectureDto.setName(name);
            externalLectureDto.setTime(finalTime);
            externalLectureDto.setCredit(credit);

            if(time.isEmpty()) {
                continue;
            }

            externalLectureDtos.add(externalLectureDto);
        }
        return externalLectureDtos;
    }

    // 에타의 모든 시간표 가져오기(조회만)
    public static List<CreateTimetableWithExternalLecturesDto> findAllEveryTimetable(String url) throws IOException {

        // 에타 시간표id 추출
        String[] parts = url.split("/");
        String identifier = parts[parts.length - 1].replace("@", "");

        // 에타 서버에 요청 보내기
        Document doc = Jsoup.connect("https://api.everytime.kr/find/timetable/table/friend")
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://everytime.kr/")
                .data("identifier", identifier)
                .data("friendInfo", "true")
                .timeout(5000)
                .post();

        // 에타에 저장된 모든 시간표id, 학년도 정보
        Elements primaryTables = doc.select("primaryTable");
        // 조회할 시간표
        List<CreateTimetableWithExternalLecturesDto> createTimetableWithExternalLecturesDtos = new ArrayList<>();

        for (Element primaryTable : primaryTables) {

            String year = primaryTable.attr("year");
            String semester = primaryTable.attr("semester");
            identifier = primaryTable.attr("identifier");

            List<ExternalLectureDto> externalLectureDtos = findByEveryTimetableLectureById(identifier);

            CreateTimetableWithExternalLecturesDto createTimetableWithExternalLecturesDto = new CreateTimetableWithExternalLecturesDto();
            createTimetableWithExternalLecturesDto.setYear(year);
            createTimetableWithExternalLecturesDto.setSemester(semester);
            createTimetableWithExternalLecturesDto.setRepresent(false); // 일단 조회만 하니까 메인시간표로 설정X
            createTimetableWithExternalLecturesDto.setLectures(externalLectureDtos);

            createTimetableWithExternalLecturesDtos.add(createTimetableWithExternalLecturesDto);
        }
        return createTimetableWithExternalLecturesDtos;
    }

}
