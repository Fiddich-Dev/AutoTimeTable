package org.fiddich.coreinfradomain.domain.Member.repository;

import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.Member.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
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


    public Optional<Member> findByStudentIdAndSchool(String studentId, String school) {

        List<Member> members = em.createQuery("select m from Member m where m.studentId = :studentId and m.school = :school", Member.class)
                .setParameter("studentId", studentId)
                .setParameter("school", school)
                .getResultList();

        return members.stream().findFirst();
    }


    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public void deleteById(Long id) {
        Member member = em.find(Member.class, id);
        if(member != null) {
            em.remove(member);
        }
    }

    public List<Member> findFriendshipRequest(Long memberId) {
        List<Member> requester = em.createQuery("select f.requester from Friendship f where f.receiver.id = :receiverId", Member.class)
                .setParameter("receiverId", memberId)
                .getResultList();

        List<Member> ret = new ArrayList<>();
        ret.addAll(requester);
        return ret;
    }

}
