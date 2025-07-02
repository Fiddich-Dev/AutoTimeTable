package com.fiddich.LectureMapping.nouse;

import com.fiddich.LectureMapping.entity.Category;
import com.fiddich.LectureMapping.entity.Lecture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LectureService {
    private static final String LECTURE_API_URL = "https://api.everytime.kr/find/timetable/subject/list";
    private static final int DELAY_MS = 200;

    @PersistenceContext
    private final EntityManager em;
    private final CategoryService categoryService;
    private final SchoolService schoolService;

    @Transactional
    public void fetchAndSaveAllLectures(String year, String semester, int batchSize) throws IOException {
        List<Category> childCategories = getChildCategories(year, semester);

        for (Category category : childCategories) {
            System.out.printf("Processing category: %s%n", category.getName());
            int totalSaved = processCategoryLectures(category, year, semester, batchSize);
            System.out.printf("Saved %d lectures for category: %s%n", totalSaved, category.getName());
        }

        System.out.println("모든 자식 카테고리 강의 저장 완료");
    }

    private List<Category> getChildCategories(String year, String semester) {
        return em.createQuery(
                        "SELECT c FROM Category c WHERE c.parent IS NOT NULL AND c.year = :year AND c.semester = :semester",
                        Category.class)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getResultList();
    }

    private int processCategoryLectures(Category category, String year, String semester, int batchSize)
            throws IOException {
        int totalSaved = 0;
        int currentStart = 0;
        boolean hasMoreData = true;

        while (hasMoreData) {
            List<Lecture> lectures = fetchLectures(
                    category.getId(),
                    category.getCampusId(),
                    year,
                    semester,
                    batchSize,
                    currentStart
            );

            if (lectures.isEmpty()) {
                hasMoreData = false;
            } else {
                saveLectures(lectures, category, year, semester);
                totalSaved += lectures.size();
                currentStart += batchSize;
                em.flush();
                sleepSafely();
            }
        }

        return totalSaved;
    }

    private List<Lecture> fetchLectures(Long categoryId, Long campusId,
                                        String year, String semester,
                                        int limit, int start) throws IOException {
        Document doc = Jsoup.connect(LECTURE_API_URL)
                .method(Connection.Method.POST)
                .headers(getCommonHeaders())
                .cookies(getCommonCookies())
                .data(createLectureRequestData(categoryId, campusId, year, semester, limit, start))
                .post();

        return doc.select("subject").stream()
                .map(this::parseLecture)
                .toList();
    }

    private Lecture parseLecture(Element subject) {
        Lecture lecture = new Lecture();
        lecture.setCode(subject.attr("code").split("-")[0]);
        lecture.setCodeSection(subject.attr("code"));
        lecture.setName(subject.attr("name"));
        lecture.setProfessor(subject.attr("professor"));
        lecture.setType(subject.attr("type"));
        lecture.setTime(parseTime(subject.attr("time")));
        lecture.setPlace(subject.attr("place"));
        lecture.setCredit(subject.attr("credit"));
        lecture.setTarget(subject.attr("target"));
        lecture.setNotice(subject.attr("notice"));
        return lecture;
    }

    private void saveLectures(List<Lecture> lectures, Category category, String year, String semester) {
        lectures.forEach(lecture -> {
            lecture.setCategory(category);
            lecture.setYear(year);
            lecture.setSemester(semester);
            lecture.setSchool(schoolService.getCurrentSchool());
            em.persist(lecture);
        });
    }

    private String parseTime(String input) {
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

    private void sleepSafely() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, String> getCommonHeaders() {
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

    private Map<String, String> getCommonCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("_ga", "GA1.1.1955426778.1751110516");
        cookies.put("x-et-device", "9970041");
        cookies.put("etsid", "s%3ACueX3hGHlZ5SwbOQB5dJtbaNtrJNLXc-.iK%2FK1fxt25gcHSLBHXLv0SKiA25590hFld4mOmECkmw");
        cookies.put("_ga_85ZNEFVRGL", "GS2.1.s1751268570$o4$g1$t1751269458$j60$l0$h0");
        return cookies;
    }

    private Map<String, String> createLectureRequestData(Long categoryId, Long campusId,
                                                         String year, String semester,
                                                         int limit, int start) {
        Map<String, String> data = new HashMap<>();
        data.put("campusId", campusId.toString());
        data.put("year", year);
        data.put("semester", semester);
        data.put("limitNum", String.valueOf(limit));
        data.put("startNum", String.valueOf(start));
        data.put("categoryId", categoryId.toString());
        return data;
    }
}
