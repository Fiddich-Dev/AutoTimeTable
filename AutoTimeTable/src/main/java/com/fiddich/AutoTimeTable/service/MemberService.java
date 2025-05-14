package com.fiddich.AutoTimeTable.service;

import com.fiddich.AutoTimeTable.entity.Friendship;
import com.fiddich.AutoTimeTable.entity.FriendshipStatus;
import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.entity.Timetable;
import com.fiddich.AutoTimeTable.repository.FriendshipRepository;
import com.fiddich.AutoTimeTable.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

    public Long save(Member member) {
        memberRepository.save(member);
        return member.getId();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public List<Member> findFriendshipRequest(Long id) {
        return memberRepository.findFriendshipRequest(id);
    }

    // 친구 요청 보내기
    public void sendFriendRequest(Member requester, Member receiver) {

        Friendship friendship = Friendship.builder()
                .friendshipStatus(FriendshipStatus.PENDING)
                .requester(requester)
                .receiver(receiver)
                .build();

        friendship.sendFriendshipRequest(requester, receiver);

        friendshipRepository.saveFriendship(friendship);
    }

    // 친구요청 수락
    public void acceptFriendRequest(Member requester, Member receiver) {

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester, receiver);
        friendshipRepository.acceptFriendRequest(friendship);
    }

    // 친구요청 거절
    public void rejectFriendRequest(Member requester, Member receiver) {
        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester, receiver);
        friendshipRepository.rejectFriendRequest(friendship);
    }

    // 수락한 친구들 조회
    public List<Member> findAcceptedRequests(Member receiver) {
        return friendshipRepository.findAcceptedRequests(receiver);
    }

    // 수락 대기중인 친구들 조회
    public List<Member> findPendingRequests(Member receiver) {
        return friendshipRepository.findPendingRequests(receiver);
    }

    // 내가 보낸요청 보기
    public List<Member> findPendingMyRequest(Member requester) {
        return friendshipRepository.findPendingMyRequest(requester);
    }


}
