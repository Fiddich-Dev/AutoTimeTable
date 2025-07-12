//package com.fiddich.LectureMapping;
//
//import com.fiddich.LectureMapping.entity.Category;
//import com.fiddich.LectureMapping.entity.Department;
//import com.fiddich.LectureMapping.entity.Lecture;
//import com.fiddich.LectureMapping.entity.School;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lombok.RequiredArgsConstructor;
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//@RequiredArgsConstructor
//public class LectureDataInitializer {
//
//    private final SchoolsProperties schoolsProperties;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @Value("${sungkyunkwan.lectures.department}")
//    private List<String> departments;
//
//    School school;
//
//    @EventListener(ApplicationReadyEvent.class)
//    @Transactional
//    public void init() throws Exception {
//
////        School school = new School();
////        school.setId(13L);
////        school.setName("성균관대학교");
////        this.school = school;
////        em.persist(school);
//
//
//        getCategory("2025", "1");
//
//        fetchAndSaveLecturesForChildCategories("2025", "1", 50, 0);
//
//
//
////        for(String department : departments) {
////            InputStream is = getClass().getClassLoader().getResourceAsStream(department + "Lectures.txt");
////            if (is == null) throw new RuntimeException("파일을 찾을 수 없습니다.");
////
////            List<String> lines = new BufferedReader(new InputStreamReader(is)).lines().toList();
////
////            for (String line : lines) {
////                Lecture lecture = LectureParser.parseLecture(line);
////                lecture.setDepartment(department);
////                em.persist(lecture);
////                System.out.println(lecture);
////            }
////        }
//
//    }
//
////    @Transactional
//    public void getCategory(String year, String semester) throws IOException {
//        Connection connection = Jsoup.connect("https://api.everytime.kr/find/timetable/subject/filter/list")
//                .method(Connection.Method.POST)
//                .header("accept", "*/*")
//                .header("accept-encoding", "gzip, deflate, br, zstd")
//                .header("accept-language", "ko,en-US;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6")
//                .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
//                .header("origin", "https://everytime.kr")
//                .header("referer", "https://everytime.kr/")
//                .header("sec-ch-ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
//                .header("sec-ch-ua-mobile", "?0")
//                .header("sec-ch-ua-platform", "\"macOS\"")
//                .header("sec-fetch-dest", "empty")
//                .header("sec-fetch-mode", "cors")
//                .header("sec-fetch-site", "same-site")
//                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
//                .cookie("_ga", "GA1.1.1955426778.1751110516")
//                .cookie("x-et-device", "9970041")
//                .cookie("etsid", "s%3ACueX3hGHlZ5SwbOQB5dJtbaNtrJNLXc-.iK%2FK1fxt25gcHSLBHXLv0SKiA25590hFld4mOmECkmw")
//                .cookie("_ga_85ZNEFVRGL", "GS2.1.s1751268570$o4$g1$t1751269458$j60$l0$h0")
//                .data("year", year)
//                .data("semester", semester);
//
//        Document doc = connection.post();
//
//        Element campus = doc.selectFirst("campus");
//        if (campus == null) {
//            System.out.println("campus 엘리먼트 없음");
//            return;
//        }
//
//        Long campusId = null;
//        String campusIdStr = campus.attr("id");
//        if (!campusIdStr.isEmpty()) {
//            campusId = Long.valueOf(campusIdStr);
//
//            School existingSchool = em.find(School.class, campusId);
//            if (existingSchool != null) {
//                this.school = existingSchool;
//            } else {
//                School school = new School();
//                school.setId(campusId);
//                school.setName("성균관대학교");
//                em.persist(school);
//                this.school = school;
//            }
//        }
//
//        Elements categories = campus.select("categories > category");
//        Map<Long, Category> categoryMap = new HashMap<>();
//
//        for (Element categoryEl : categories) {
//            Long id = Long.valueOf(categoryEl.attr("id"));
//            String name = categoryEl.attr("name");
//            int orderNum = Integer.parseInt(categoryEl.attr("order"));
//
//            Category category = new Category(id, name, orderNum);
//            category.setYear(year);
//            category.setSemester(semester);
//            category.setCampusId(campusId);
//            categoryMap.put(id, category);
//        }
//
//        for (Element categoryEl : categories) {
//            String parentIdStr = categoryEl.attr("parentId");
//            if (!parentIdStr.isEmpty()) {
//                Long id = Long.valueOf(categoryEl.attr("id"));
//                Long parentId = Long.valueOf(parentIdStr);
//                Category child = categoryMap.get(id);
//                Category parent = categoryMap.get(parentId);
//                child.setParent(parent);
//                parent.getChildren().add(child);
//            }
//        }
//
//        for (Category category : categoryMap.values()) {
//            if (category.getParent() == null) {
//                em.persist(category);
//            }
//        }
//
//        em.flush();
//        System.out.println("카테고리 저장 완료 - " + year + "년 " + semester + "학기");
//    }
//
//    public void getDepartment() {
//
//    }
//
//    public void fetchAndSaveLecturesForChildCategories(String year, String semester, int limitNum, int startNum) throws IOException {
//        // 1. parent_id != null 인 Category 리스트 조회
//        List<Category> childCategories = em.createQuery(
//                        "SELECT c FROM Category c WHERE c.parent IS NOT NULL AND c.year = :year AND c.semester = :semester", Category.class)
//                .setParameter("year", year)
//                .setParameter("semester", semester)
//                .getResultList();
//
//        for (Category category : childCategories) {
//            Long categoryId = category.getId();
//            Long campusId = category.getCampusId();
//            int currentStartNum = 0;
//            boolean hasMoreData = true;
//            int totalSaved = 0;
//
//            System.out.println("Processing category: " + category.getName());
//
//            // 2. 페이지네이션으로 모든 강의 조회
//            while (hasMoreData) {
//                List<Lecture> lectures = fetchLectures(categoryId, campusId, year, semester, limitNum, currentStartNum);
//
//                if (lectures.isEmpty()) {
//                    hasMoreData = false;
//                } else {
//                    // 3. 강의 저장
//                    for (Lecture lecture : lectures) {
//                        lecture.setCategory(category);
//                        lecture.setYear(year);
//                        lecture.setSemester(semester);
//                        lecture.setSchool(this.school);
//                        em.persist(lecture);
//                    }
//
//                    totalSaved += lectures.size();
//                    currentStartNum += limitNum;
//
//                    // 트랜잭션 플러시 및 약간의 지연 추가 (서버 부하 방지)
//                    em.flush();
//                    try {
//                        Thread.sleep(200); // 0.2초 지연
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//
//            System.out.println("Saved " + totalSaved + " lectures for category: " + category.getName());
//        }
//
//        System.out.println("모든 자식 카테고리 강의 저장 완료");
//    }
//
//    // 강의 데이터를 가져오는 메서드 예시
//    private List<Lecture> fetchLectures(Long categoryId, Long campusId, String year, String semester, int limitNum, int startNum) throws IOException {
//        List<Lecture> lectures = new ArrayList<>();
//
//        String url = "https://api.everytime.kr/find/timetable/subject/list";
//
//        Connection connection = Jsoup.connect(url)
//                .method(Connection.Method.POST)
//                .header("accept", "*/*")
//                .header("accept-encoding", "gzip, deflate, br, zstd")
//                .header("accept-language", "ko,en-US;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6")
//                .header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
//                .header("origin", "https://everytime.kr")
//                .header("referer", "https://everytime.kr/")
//                .header("sec-ch-ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
//                .header("sec-ch-ua-mobile", "?0")
//                .header("sec-ch-ua-platform", "\"macOS\"")
//                .header("sec-fetch-dest", "empty")
//                .header("sec-fetch-mode", "cors")
//                .header("sec-fetch-site", "same-site")
//                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
//                .cookie("_ga", "GA1.1.1955426778.1751110516")
//                .cookie("x-et-device", "9970041")
//                .cookie("etsid", "s%3ACueX3hGHlZ5SwbOQB5dJtbaNtrJNLXc-.iK%2FK1fxt25gcHSLBHXLv0SKiA25590hFld4mOmECkmw")
//                .cookie("_ga_85ZNEFVRGL", "GS2.1.s1751268570$o4$g1$t1751269458$j60$l0$h0")
//                .data("campusId", campusId.toString())
//                .data("year", year)
//                .data("semester", semester)
//                .data("limitNum", String.valueOf(limitNum))
//                .data("startNum", String.valueOf(startNum))
//                .data("categoryId", categoryId.toString());
//
//        Document doc = connection.post();
//
//        Elements subjectElements = doc.select("subject");
//
//        for (Element subject : subjectElements) {
//            Lecture lecture = new Lecture();
//
//            // lecture_id는 DB에서 자동 생성하는 ID이고, XML의 id와는 다를 수 있으니 주의
//            // 그래서 XML 속성 id는 매핑하지 않았고, lectureId 속성은 Long 변환해서 매핑
//            lecture.setCode(subject.attr("code").split("-")[0]);
//            lecture.setCodeSection(subject.attr("code"));
//            lecture.setName(subject.attr("name"));
//            lecture.setProfessor(subject.attr("professor"));
//            lecture.setType(subject.attr("type"));
//            lecture.setTime(timeParse(subject.attr("time")));
//            lecture.setPlace(subject.attr("place"));
//            lecture.setCredit(subject.attr("credit"));
//            lecture.setTarget(subject.attr("target"));
//            lecture.setNotice(subject.attr("notice"));
//
////            // lectureId는 DB PK가 아니므로 id에 직접 넣지 말고 externalLectureId에 넣기
////            String lectureIdStr = subject.attr("lectureId");
////            if (!lectureIdStr.isEmpty()) {
////                try {
////                    lecture.setExternalLectureId(Long.parseLong(lectureIdStr));
////                } catch (NumberFormatException e) {
////                    e.printStackTrace();
////                }
////            }
//
//            // isCustom, year, semester, memberId 는 XML에 없으므로 기본값 유지 또는 별도 처리 필요
//
//            // school, category는 ManyToOne 관계이므로 XML에 관련 정보가 있으면 따로 처리 필요
//            // 여기서는 설정 안 함
//
//            lectures.add(lecture);
//        }
//
//        return lectures;
//    }
//
//    public String timeParse(String input) {
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
//}
//
//
//
//
//
