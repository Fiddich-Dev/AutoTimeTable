package org.fiddich.coreinfradomain.domain.friendship.repository;


import jakarta.persistence.EntityManager;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Member.Member;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FriendshipRepository {

    private final EntityManager em;

    // 수락 대기중인 친구들 조회
    public List<Member> findPendingRequests(Member receiver) {
        return em.createQuery("select f.requester from Friendship f where f.receiver = :receiver and f.friendshipStatus = :status", Member.class)
                .setParameter("receiver", receiver)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
    }

    // 보낸 요청중 보류중인거
    public List<Member> findPendingRequest(Long requesterId) {
        List<Member> requestFriends = em.createQuery("select f.receiver from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = :status", Member.class)
                .setParameter("requesterId", requesterId)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
        return requestFriends;
    }


}
