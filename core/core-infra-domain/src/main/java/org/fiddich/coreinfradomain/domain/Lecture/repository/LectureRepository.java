package org.fiddich.coreinfradomain.domain.Lecture.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LectureRepository {

    private final EntityManager em;

    public Optional<Lecture> findById(Long id) {
        Lecture lecture = em.find(Lecture.class, id);
        return Optional.ofNullable(lecture);
    }

    public List<Lecture> findByDepartment(String categoryName) {
        return em.createQuery("select l from Lecture l where l.category.name = :categoryName", Lecture.class)
                .setParameter("categoryName", categoryName)
                .getResultList();
    }

    public List<Lecture> findAllByCategoryIds(List<Long> categoryIds) {
        return em.createQuery("select l from Lecture l where l.category.id in :categoryIds", Lecture.class)
                .setParameter("categoryIds", categoryIds)
                .getResultList();
    }

    public List<Lecture> findAllByCategoryIdsWithParentCategory(List<Long> categoryIds) {
        return em.createQuery("select l from Lecture l join fetch l.category c where c.id in :categoryIds", Lecture.class)
                .setParameter("categoryIds", categoryIds)
                .getResultList();
    }

//    public List<Lecture> findByIds(List<Long> ids) {
//        return em.createQuery("select l from Lecture l where l.id in :ids", Lecture.class)
//                .setParameter("ids", ids)
//                .getResultList();
//    }

    public Optional<Lecture> findByCodeSection(String codeSection) {
        List<Lecture> result = em.createQuery("select l from Lecture l where l.codeSection = :codeSection", Lecture.class)
                .setParameter("codeSection", codeSection)
                .getResultList();
        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result.get(0));
        }
    }

    public List<Lecture> findAll() {
        return em.createQuery("select l from Lecture l", Lecture.class)
                .getResultList();
    }

    public List<Lecture> findAllByIds(List<Long> lectureIds) {
        return em.createQuery("select l from Lecture l where l.id in :lectureIds", Lecture.class)
                .setParameter("lectureIds", lectureIds)
                .getResultList();
    }

    public Long save(Lecture lecture) {
        em.persist(lecture);
        return lecture.getId();
    }

    public List<Lecture> searchByAutoField(String keyword) {
        return em.createQuery("""
        select l from Lecture l
        where l.name like :kw
           or l.professor like :kw
           or l.codeSection like :kw
    """, Lecture.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
    }

}
