package org.fiddich.coreinfradomain.domain.friendship.repository;


import jakarta.persistence.EntityManager;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Member.Member;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipRepository {


    private final EntityManager em;

    public Friendship findByRequesterAndReceiver(Member requester, Member receiver) {
        return em.createQuery("select f from Friendship f where f.requester = :requester and f.receiver = :receiver", Friendship.class)
                .setParameter("requester", requester)
                .setParameter("receiver", receiver)
                .getSingleResult();
    }

    // 친구요청 보내기
    public void saveFriendship(Friendship friendship) {
        em.persist(friendship);
    }

    // 친구요청 수락
    public void acceptFriendRequest(Friendship friendship) {
        friendship.accept();
    }

    // 친구요청 거절
    public void rejectFriendRequest(Friendship friendship) {
        em.remove(friendship);
    }

    // 수락한 친구들 조회
    public List<Member> findAcceptedRequests(Member receiver) {
        return em.createQuery("select f.requester from Friendship f where f.receiver = :receiver and f.friendshipStatus = :status", Member.class)
                .setParameter("receiver", receiver)
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();
    }

    // 수락 대기중인 친구들 조회
    public List<Member> findPendingRequests(Member receiver) {
        return em.createQuery("select f.requester from Friendship f where f.receiver = :receiver and f.friendshipStatus = :status", Member.class)
                .setParameter("receiver", receiver)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
    }

    // 보낸 친구요청 보기
    public List<Member> findPendingMyRequest(Member requester) {
        return em.createQuery("select f.receiver from Friendship f where f.requester = :requester and f.friendshipStatus = :status", Member.class)
                .setParameter("requester", requester)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
    }

}
