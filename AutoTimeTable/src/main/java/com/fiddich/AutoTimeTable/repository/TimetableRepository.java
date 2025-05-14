package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Timetable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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

}
