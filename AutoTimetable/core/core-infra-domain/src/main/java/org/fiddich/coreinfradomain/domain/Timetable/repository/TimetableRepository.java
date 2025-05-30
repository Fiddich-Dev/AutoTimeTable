package org.fiddich.coreinfradomain.domain.Timetable.repository;


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@RequiredArgsConstructor
public class TimetableRepository {

    private final EntityManager em;

    public void save(Timetable timetable) {
        em.persist(timetable);
    }

    public Timetable findById(Long id) {
        return em.find(Timetable.class, id);
    }

    public List<Timetable> findAll() {
        return em.createQuery("select t from Timetable t", Timetable.class)
                .getResultList();
    }

    public List<Timetable> findByMember(Long memberId) {
        return em.createQuery("select distinct t from Timetable t where t.member.id = :memberId", Timetable.class)
                .setParameter("memberId", memberId)
                .getResultList();

    }

    public List<Timetable> findTimetablesWithLecturesByMemberId(Long memberId) {

        String jpql = """
        SELECT DISTINCT t
        FROM Timetable t
        LEFT JOIN FETCH t.timetableLectures
        WHERE t.member.id = :memberId
    """;

        return em.createQuery(jpql, Timetable.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }


}
