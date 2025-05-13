package com.fiddich.AutoTimeTable.service;

import com.fiddich.AutoTimeTable.entity.Friendship;
import com.fiddich.AutoTimeTable.entity.FriendshipStatus;
import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.entity.Timetable;
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

    public void saveFriendship(Long requesterId, Long receiverId) {
        Friendship friendship = new Friendship();
        Member requester = findById(requesterId);
        Member receiver = findById(receiverId);
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);
        friendship.setFriendshipStatus(FriendshipStatus.PENDING);
        memberRepository.saveFriendship(friendship);
    }

    public List<Member> findFriendshipRequest(Long id) {
        return memberRepository.findFriendshipRequest(id);
    }

}
