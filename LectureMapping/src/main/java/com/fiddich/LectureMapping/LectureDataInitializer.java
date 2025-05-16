package com.fiddich.LectureMapping;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class LectureDataInitializer {

    @PersistenceContext
    private EntityManager em;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("cultureLectures.txt");
        if (is == null) throw new RuntimeException("파일을 찾을 수 없습니다.");

        List<String> lines = new BufferedReader(new InputStreamReader(is)).lines().toList();

        for (String line : lines) {
            CultureLecture lecture = LectureParser.parseLecture(line);
            em.persist(lecture);
            System.out.println(lecture);
        }
    }
}

