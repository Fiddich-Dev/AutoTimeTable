package org.fiddich.coreinfradomain.domain.Member.repository;

import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.Lecture.School;
import org.fiddich.coreinfradomain.domain.Member.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public Optional<Member> findByStudentId(String studentId) {

        Member member = em.createQuery("select m from Member m where m.studentId = :studentId", Member.class)
                .setParameter("studentId", studentId)
                .getSingleResult();

        return Optional.ofNullable(member);
    }

    public List<Member> findByStudentIdContaining(String keyword) {
        return em.createQuery("select m from Member m where m.studentId like :keyword", Member.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }


    public void deleteById(Long id) {
        Member member = em.find(Member.class, id);
        if(member != null) {
            em.remove(member);
        }
    }

}
