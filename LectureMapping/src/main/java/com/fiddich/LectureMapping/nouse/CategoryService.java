package com.fiddich.LectureMapping.nouse;

import com.fiddich.LectureMapping.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private static final String CATEGORY_API_URL = "https://api.everytime.kr/find/timetable/subject/filter/list";

    @PersistenceContext
    private final EntityManager em;
    private final SchoolService schoolService;

    @Transactional
    public void fetchAndSaveCategories(String year, String semester) throws IOException {
        Document doc = fetchCategoryDocument(year, semester);
        Element campus = doc.selectFirst("campus");

        if (campus == null) {
            throw new IllegalStateException("Campus element not found");
        }

        processCategories(campus, year, semester);
    }

    private Document fetchCategoryDocument(String year, String semester) throws IOException {
        return Jsoup.connect(CATEGORY_API_URL)
                .method(Connection.Method.POST)
                .headers(getCommonHeaders())
                .cookies(getCommonCookies())
                .data("year", year)
                .data("semester", semester)
                .post();
    }

    private void processCategories(Element campus, String year, String semester) {
        Long campusId = Long.valueOf(campus.attr("id"));
        Map<Long, Category> categoryMap = new HashMap<>();

        // 카테고리 생성 및 매핑
        Elements categoryElements = campus.select("categories > category");
        categoryElements.forEach(el -> createAndMapCategory(el, categoryMap, year, semester, campusId));

        // 부모-자식 관계 설정
        categoryElements.forEach(el -> setParentChildRelationship(el, categoryMap));

        // 최상위 카테고리 저장
        categoryMap.values().stream()
                .filter(c -> c.getParent() == null)
                .forEach(em::persist);

        em.flush();
        System.out.printf("카테고리 저장 완료 - %s년 %s학기%n", year, semester);
    }

    private void createAndMapCategory(Element el, Map<Long, Category> map,
                                      String year, String semester, Long campusId) {
        Category category = new Category(
                Long.valueOf(el.attr("id")),
                el.attr("name"),
                Integer.parseInt(el.attr("order"))
        );
        category.setYear(year);
        category.setSemester(semester);
        category.setCampusId(campusId);
        map.put(category.getId(), category);
    }

    private void setParentChildRelationship(Element el, Map<Long, Category> map) {
        String parentIdStr = el.attr("parentId");
        if (!parentIdStr.isEmpty()) {
            Long id = Long.valueOf(el.attr("id"));
            Long parentId = Long.valueOf(parentIdStr);
            Category child = map.get(id);
            Category parent = map.get(parentId);
            child.setParent(parent);
            parent.getChildren().add(child);
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
}
