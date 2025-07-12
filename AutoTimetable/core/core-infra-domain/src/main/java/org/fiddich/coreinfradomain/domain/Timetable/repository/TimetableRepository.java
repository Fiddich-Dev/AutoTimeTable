package org.fiddich.coreinfradomain.domain.Timetable.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Category;
import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class TimetableRepository {

    private final EntityManager em;

    public void save(Timetable timetable) {
        em.persist(timetable);
    }

    public Optional<Timetable> findById(Long id) {
        return Optional.ofNullable(em.find(Timetable.class, id));
    }

    public Optional<Timetable> findByIdWithTimetableLectures(Long id) {

        Timetable timetable = em.createQuery(
                "select t from Timetable t " +
                "join fetch t.timetableLectures tl " +
                "join fetch tl.lecture " +
                "where t.id = :id", Timetable.class)
                .setParameter("id", id)
                .getSingleResult();

        return Optional.ofNullable(timetable);
    }

    public Optional<Timetable> findMainByIdWithTimetableLectures(Long id, String year, String semester) {

        Timetable timetable = em.createQuery(
                        "select t from Timetable t " +
                                "join fetch t.timetableLectures tl " +
                                "join fetch tl.lecture " +
                                "where t.id = :id and t.isRepresent = true and t.year = :year and t.semester = :semester", Timetable.class)
                .setParameter("id", id)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getSingleResult();

        return Optional.ofNullable(timetable);
    }

    public Timetable findMainByMemberIdWithLectures(Long memberId, String year, String semester) {
        String jpql = """
        select t from Timetable t
        join fetch t.timetableLectures tl
        join fetch tl.lecture l
        where t.member.id = :memberId
          and t.isRepresent = true
          and t.year = :year
          and t.semester = :semester
    """;

        try {
            return em.createQuery(jpql, Timetable.class)
                    .setParameter("memberId", memberId)
                    .setParameter("year", year)
                    .setParameter("semester", semester)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // 혹은 예외를 다시 던지거나 Optional.ofNullable() 처리
        }
    }

    public List<Timetable> findAll() {
        return em.createQuery("select t from Timetable t", Timetable.class)
                .getResultList();
    }

    public List<Timetable> findByMember(Long memberId) {
        return em.createQuery("select t from Timetable t where t.member.id = :memberId", Timetable.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public List<Timetable> findTimetablesWithLecturesByMemberId(Long memberId) {
        String jpql = """
        SELECT t
        FROM Timetable t
        JOIN FETCH t.timetableLectures tl
        JOIN FETCH tl.lecture l
        JOIN FETCH l.category c
        WHERE t.member.id = :memberId
    """;
        return em.createQuery(jpql, Timetable.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public void deleteTimetable(Long timetableId) {
        Timetable timetable = em.find(Timetable.class, timetableId);
        if (timetable != null) {
            em.remove(timetable);
        }
    }

    public void deleteTimetableLecture(Long timetableId) {
        em.createQuery("delete from TimetableLecture tl where tl.timetable.id = :timetableId")
                .setParameter("timetableId", timetableId)
                .executeUpdate();
    }

    public void clearMainTimetable(Long memberId) {
        em.createQuery("update Timetable t set t.isRepresent = false where t.member.id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    public void updateMainTimetable(Long timetableId) {
        em.createQuery("update Timetable t set t.isRepresent = true where t.id = :timetableId")
                .setParameter("timetableId", timetableId)
                .executeUpdate();
    }

    public List<Category> findAllCategoryByYearAndSemester(String year, String semester) {
        return em.createQuery("select c from Category c where c.year = :year and c.semester = :semester", Category.class)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getResultList();
    }




}
