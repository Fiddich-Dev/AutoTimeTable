package org.fiddich.coreinfradomain.domain.Lecture.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.CustomLecture;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.OfficialLecture;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class LectureRepository {

    private final EntityManager em;

//    public List<CustomLecture> findAllByCustomLectureIds(List<Long> customLectureIds) {
//        return em.createQuery("select cl from CustomLecture cl")
//    }

//    public List<Lecture> findAllByCategoryIdsWithParentCategory(List<Long> categoryIds) {
//        return em.createQuery("select l from Lecture l join fetch l.category c where c.id in :categoryIds", Lecture.class)
//                .setParameter("categoryIds", categoryIds)
//                .getResultList();
//    }


//    public Optional<Lecture> findByCodeSection(String codeSection, String year, String semester) {
//        List<Lecture> result = em.createQuery("select l from Lecture l where l.codeSection = :codeSection and l.category.year = :year and l.category.semester = :semester", Lecture.class)
//                .setParameter("codeSection", codeSection)
//                .setParameter("year", year)
//                .setParameter("semester", semester)
//                .getResultList();
//        if (result.isEmpty()) {
//            return Optional.empty();
//        } else {
//            return Optional.of(result.get(0));
//        }
//    }
//

    // 저장
    public void save(Lecture lecture) {
        em.persist(lecture);
    }



}
