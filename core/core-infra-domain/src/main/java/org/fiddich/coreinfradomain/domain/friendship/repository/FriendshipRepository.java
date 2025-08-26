package org.fiddich.coreinfradomain.domain.friendship.repository;


import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("select fs from Friendship fs where fs.receiver.id = :memberId or fs.requester.id = :memberId")
    List<Friendship> findAllByMemberId(@Param("memberId") Long memberId);

    // 나에게 친구 요청을 보냈는데 수락 대기중인 요청들
    @Query("select f.requester from Friendship f where f.receiver = :receiver and f.friendshipStatus = 'PENDING'")
    List<Member> findPendingRequestsReceivedBy(@Param("receiver") Member receiver);

    // 내가 친구 요청을 보냈는데 수락 대기중인 요청들
    @Query("select f.receiver from Friendship f where f.requester.id = :requesterId and f.friendshipStatus = 'PENDING'")
    List<Member> findPendingRequestsSentBy(@Param("requesterId") Long requesterId);
}
