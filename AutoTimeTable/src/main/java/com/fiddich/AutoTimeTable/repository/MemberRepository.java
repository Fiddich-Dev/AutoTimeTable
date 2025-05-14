package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Friendship;
import com.fiddich.AutoTimeTable.entity.FriendshipStatus;
import com.fiddich.AutoTimeTable.entity.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findById(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
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
