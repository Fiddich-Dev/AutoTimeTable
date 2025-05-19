package com.fiddich.LectureMapping;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${sungkyunkwan.lectures.department}")
    private List<String> departments;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() throws Exception {

        for(String department : departments) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(department + "Lectures.txt");
            if (is == null) throw new RuntimeException("파일을 찾을 수 없습니다.");

            List<String> lines = new BufferedReader(new InputStreamReader(is)).lines().toList();

            for (String line : lines) {
                Lecture lecture = LectureParser.parseLecture(line);
                lecture.setDepartment(department);
                em.persist(lecture);
                System.out.println(lecture);
            }
        }

    }
}




