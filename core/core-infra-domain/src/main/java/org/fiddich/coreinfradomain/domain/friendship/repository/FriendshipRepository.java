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

    // 친구 요청 중복 확인
    public Optional<Friendship> findByRequesterAndReceiver(Long requesterId, Long receiverId) {
        List<Friendship> friendships = em.createQuery("select f from Friendship f where f.requester.id = :requesterId and f.receiver.id = :receiverId", Friendship.class)
                .setParameter("requesterId", requesterId)
                .setParameter("receiverId", receiverId)
                .getResultList();

        return friendships.stream().findFirst();
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

    // 내 id가 보낸사람과 받는 사람에 있는 친구들 중 accept상태인 사람들
    public List<Member> findAllFriends(Long id) {
        // 내가 보낸 요청중 수락된거
        List<Member> requestFriends = em.createQuery("select f.receiver from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = :status", Member.class)
                .setParameter("requesterId", id)
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();
        // 내가 받은 요청중 수락된거
        List<Member> receivedFriends = em.createQuery("select f.requester from Friendship f where f.receiver.id = :receiverId and f.friendshipStatus = :status", Member.class)
                .setParameter("receiverId", id)
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();

        List<Member> allFriends = new ArrayList<>();
        allFriends.addAll(requestFriends);
        allFriends.addAll(receivedFriends);
        return allFriends;
    }

    // 받은 요청중 보류중인거
    public List<Member> findPendingResponse(Long receiverId) {
        List<Member> receivedFriends = em.createQuery("select f.requester from Friendship f where f.receiver.id = :receiverId and f.friendshipStatus = :status", Member.class)
                .setParameter("receiverId", receiverId)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
        return receivedFriends;
    }

    // 보낸 요청중 보류중인거
    public List<Member> findPendingRequest(Long requesterId) {
        List<Member> requestFriends = em.createQuery("select f.receiver from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = :status", Member.class)
                .setParameter("requesterId", requesterId)
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
        return requestFriends;
    }

    // 친구 삭제
    public void deleteFriend(Long myId, Long friendId) {
        em.createQuery("delete from Friendship f where f.receiver.id = :myId and f.requester.id = :friendId")
                .setParameter("myId", myId)
                .setParameter("friendId", friendId)
                .executeUpdate();

        em.createQuery("delete from Friendship f where f.receiver.id = :friendId and f.requester.id = :myId")
                .setParameter("friendId", friendId)
                .setParameter("myId", myId)
                .executeUpdate();
    }

}
