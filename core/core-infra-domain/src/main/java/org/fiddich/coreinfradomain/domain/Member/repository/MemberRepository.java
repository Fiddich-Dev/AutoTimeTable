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


    public Optional<Member> findByStudentIdAndSchool(String studentId, String school) {

        List<Member> members = em.createQuery("select m from Member m where m.studentId = :studentId and m.school = :school", Member.class)
                .setParameter("studentId", studentId)
                .setParameter("school", school)
                .getResultList();

        return members.stream().findFirst();
    }

    public Optional<Member> findByStudentId(String studentId) {

        List<Member> members = em.createQuery("select m from Member m where m.studentId = :studentId", Member.class)
                .setParameter("studentId", studentId)
                .getResultList();

        return members.stream().findFirst();
    }

    public List<Member> findByStudentIdContaining(String keyword) {
        return em.createQuery("select m from Member m where m.studentId like :keyword", Member.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
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

    // 내 id가 보낸사람과 받는 사람에 있는 친구들 중 accept상태인 사람들
    public List<Member> findAllFriends(Long id) {
        // 내가 보낸 요청중 수락된거
        List<Member> requestFriends = em.createQuery("select f.requester from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = :status", Member.class)
                .setParameter("requesterId", id)
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();
        // 내가 받은 요청중 수락된거
        List<Member> receivedFriends = em.createQuery("select f.requester from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = :status", Member.class)
                .setParameter("requesterId", id)
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();

        List<Member> allFriends = new ArrayList<>();
        allFriends.addAll(requestFriends);
        allFriends.addAll(receivedFriends);
        return allFriends;
    }

    public School findSchoolByName(String name) {
        return em.createQuery("select s from School s where s.name = :name", School.class)
                .setParameter("name", name)
                .getSingleResult();
    }

}
