package org.fiddich.coreinfradomain.domain.Lecture.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Department;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LectureRepository {

    private final EntityManager em;

    public List<Lecture> findByDepartment(Department department) {
        return em.createQuery("select l from Lecture l where l.department = :department", Lecture.class)
                .setParameter("department", department)
                .getResultList();
    }

}
