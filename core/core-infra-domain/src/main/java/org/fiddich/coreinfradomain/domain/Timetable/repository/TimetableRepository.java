package org.fiddich.coreinfradomain.domain.Timetable.repository;

import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    @EntityGraph(attributePaths = {"customLectures", "officialLectures"})
    @Query("select distinct t from Timetable t where t.id = :id")
    Optional<Timetable> findByIdWithLectures(@Param("id") Long id);

    @EntityGraph(attributePaths = {"customLectures", "officialLectures"})
    @Query("SELECT DISTINCT t FROM Timetable t WHERE t.member.id = :memberId AND t.year = :year AND t.semester = :semester AND t.isRepresent = true")
    Optional<Timetable> findMainByMemberIdWithLectures(
            @Param("memberId") Long memberId,
            @Param("year") String year,
            @Param("semester") String semester);

    List<Timetable> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"customLectures", "officialLectures"})
    @Query("SELECT DISTINCT t FROM Timetable t WHERE t.member.id = :memberId AND t.year = :year AND t.semester = :semester")
    List<Timetable> findByMemberIdWithLectures(Long memberId, String year, String semester);

    @Modifying
    @Query("update Timetable t set t.isRepresent = false where t.member.id = :memberId and t.year = :year and t.semester = :semester")
    void resetMainTimetableByMemberId(@Param("memberId") Long memberId, @Param("year") String year, @Param("semester") String semester);

    @Modifying
    @Query("update Timetable t set t.isRepresent = true where t.id = :timetableId")
    void setMainTimetableById(@Param("timetableId") Long timetableId);


    List<Timetable> findAllByMemberId(Long memberId);
}

//@Repository
//@Transactional
//@RequiredArgsConstructor
//public class TimetableRepository {
//
//    private final EntityManager em;
//
//
//    public void save(Timetable timetable) {
//        em.persist(timetable);
//    }
//
//    public Optional<Timetable> findById(Long timetableId) {
//        return Optional.ofNullable(em.find(Timetable.class, timetableId));
//    }
//
//    // 시간표의 공식 강의까지 가져온다
//    public Optional<Timetable> findByIdWithTimetableLectures(Long id) {
//
//        List<Timetable> timetables = em.createQuery("""
//        SELECT t FROM Timetable t
//        WHERE t.id = :id
//        """, Timetable.class)
//                .setParameter("id", id)
//                .getResultList();
//
//        if (!timetables.isEmpty()) {
//            // 2. 커스텀 강의 페치 조인
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.customLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//
//            // 3. 공식 강의 페치 조인
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.officialLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//        }
//
//        return timetables.stream().findFirst();
//    }
//
//    public Optional<Timetable> findMainByMemberIdWithLectures(Long memberId, String year, String semester) {
//        // 1. 기본 정보 조회
//        List<Timetable> timetables = em.createQuery("""
//        SELECT DISTINCT t FROM Timetable t
//        WHERE t.member.id = :memberId
//        AND t.year = :year
//        AND t.semester = :semester
//        AND t.isRepresent = true
//        """, Timetable.class)
//                .setParameter("memberId", memberId)
//                .setParameter("year", year)
//                .setParameter("semester", semester)
//                .getResultList();
//
//        if (!timetables.isEmpty()) {
//            // 2. 커스텀 강의 페치 조인
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.customLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//
//            // 3. 공식 강의 조회
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.officialLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//        }
//
//        return timetables.stream().findFirst();
//    }
//
//    public List<Timetable> findByMember(Long memberId) {
//        return em.createQuery("select t from Timetable t where t.member.id = :memberId", Timetable.class)
//                .setParameter("memberId", memberId)
//                .getResultList();
//    }
//
//    public List<Timetable> findTimetablesWithLecturesByMemberId(Long memberId, String year, String semester) {
//        // 1. 기본 타임테이블 조회
//        List<Timetable> timetables = em.createQuery("""
//        SELECT t FROM Timetable t
//        WHERE t.member.id = :memberId
//        AND t.year = :year
//        AND t.semester = :semester
//        """, Timetable.class)
//                .setParameter("memberId", memberId)
//                .setParameter("year", year)
//                .setParameter("semester", semester)
//                .getResultList();
//
//        if (!timetables.isEmpty()) {
//            // 2. 커스텀 강의 페치 조인
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.customLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//
//            // 3. 공식 강의 페치 조인
//            em.createQuery("""
//            SELECT DISTINCT t FROM Timetable t
//            LEFT JOIN FETCH t.officialLectures
//            WHERE t IN :timetables
//            """, Timetable.class)
//                    .setParameter("timetables", timetables)
//                    .getResultList();
//        }
//
//        return timetables;
//    }
//
//    public void deleteTimetable(Long timetableId) {
//        Timetable timetable = em.find(Timetable.class, timetableId);
//        if (timetable != null) {
//            em.remove(timetable);
//        }
//    }
//
//    public void clearMainTimetable(Long memberId, String year, String semester) {
//        em.createQuery("update Timetable t set t.isRepresent = false where t.member.id = :memberId and t.year = :year and t.semester = :semester")
//                .setParameter("memberId", memberId)
//                .setParameter("year", year)
//                .setParameter("semester", semester)
//                .executeUpdate();
//    }
//
//    public void updateMainTimetable(Long timetableId) {
//        em.createQuery("update Timetable t set t.isRepresent = true where t.id = :timetableId")
//                .setParameter("timetableId", timetableId)
//                .executeUpdate();
//    }
//
//
//    public void deleteAllTimetableByMemberId(Long memberId) {
//        em.createQuery("delete from Timetable t where t.member.id = :memberId")
//                .setParameter("memberId", memberId)
//                .executeUpdate();
//    }
//
//    public List<Timetable> findAllByMember(Long memberId) {
//        return em.createQuery("select t from Timetable t where t.member.id = :memberId", Timetable.class)
//                .setParameter("memberId", memberId)
//                .getResultList();
//    }
//}


