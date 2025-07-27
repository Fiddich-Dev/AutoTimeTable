package org.fiddich.coreinfradomain.domain.Lecture.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Lecture.Category;
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

    public List<Lecture> findAllByCategoryIdsWithParentCategory(List<Long> categoryIds) {
        return em.createQuery("select l from Lecture l join fetch l.category c where c.id in :categoryIds", Lecture.class)
                .setParameter("categoryIds", categoryIds)
                .getResultList();
    }


    public Optional<Lecture> findByCodeSection(String codeSection, String year, String semester) {
        List<Lecture> result = em.createQuery("select l from Lecture l where l.codeSection = :codeSection and l.category.year = :year and l.category.semester = :semester", Lecture.class)
                .setParameter("codeSection", codeSection)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getResultList();
        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(result.get(0));
        }
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

    public List<Lecture> searchByAutoField(String keyword, int page, int size) {
        return em.createQuery("""
    select l from Lecture l
    where (l.name like :kw
        or l.professor like :kw
        or l.codeSection like :kw)
      and l.member is null
""", Lecture.class)
                .setParameter("kw", "%" + keyword + "%")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Category> findAllCategoryByYearAndSemester(String year, String semester) {
        return em.createQuery("select c from Category c where c.year = :year and c.semester = :semester and c.parent is not null", Category.class)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .getResultList();
    }

    public List<Category> searchCategoryByYearAndSemester(String keyword, String year, String semester, int page, int size) {
        return em.createQuery("select c from Category c where c.year = :year and c.semester = :semester and c.parent is not null and c.name like :keyword", Category.class)
                .setParameter("year", year)
                .setParameter("semester", semester)
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public void deleteCustomLectureByTimetable(Long timetableId) {
        em.createQuery("""
    DELETE FROM TimetableLecture tl
    WHERE tl.timetable.id = :timetableId
      AND tl.lecture IN (
        SELECT l FROM Lecture l WHERE l.member IS NOT NULL
      )
""")
                .setParameter("timetableId", timetableId)
                .executeUpdate();

        em.createQuery("""
    DELETE FROM Lecture l
    WHERE l.member IS NOT NULL
      AND NOT EXISTS (
        SELECT 1 FROM TimetableLecture tl WHERE tl.lecture = l
      )
""")
                .executeUpdate();
    }

    public void deleteCustomLecturesByMember(Long memberId) {
        // 1단계: 시간표에서 lecture 삭제 (해당 member의 시간표 + 커스텀 강의)
        em.createQuery("""
        DELETE FROM TimetableLecture tl
        WHERE tl.timetable.member.id = :memberId
          AND tl.lecture IN (
              SELECT l FROM Lecture l WHERE l.member.id = :memberId
          )
    """)
                .setParameter("memberId", memberId)
                .executeUpdate();

        // 2단계: orphan된 lecture 삭제 (커스텀 강의 중, 더 이상 timetable에 포함되지 않은 것)
        em.createQuery("""
        DELETE FROM Lecture l
        WHERE l.member.id = :memberId
          AND NOT EXISTS (
              SELECT 1 FROM TimetableLecture tl WHERE tl.lecture = l
          )
    """)
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

}
