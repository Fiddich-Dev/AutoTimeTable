package org.fiddich.coreinfradomain.domain.Lecture.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Department;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LectureRepository {

    private final EntityManager em;

    public Optional<Lecture> findById(Long id) {
        Lecture lecture = em.find(Lecture.class, id);
        return Optional.ofNullable(lecture);
    }

    public List<Lecture> findByDepartment(Department department) {
        return em.createQuery("select l from Lecture l where l.department = :department", Lecture.class)
                .setParameter("department", department)
                .getResultList();
    }

}
