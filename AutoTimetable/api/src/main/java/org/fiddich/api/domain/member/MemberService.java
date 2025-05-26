package org.fiddich.api.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.ReceiveFriendshipDto;
import org.fiddich.api.domain.member.dto.RequestFriendshipDto;
import org.fiddich.api.domain.member.dto.JoinDto;
import org.fiddich.api.domain.member.dto.MemberIdentifierDto;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.friendship.repository.FriendshipRepository;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Long join(JoinDto joinDto) {

        // 학번이 안겹치는지 확인하는 로직
        if(memberRepository.findByStudentIdAndSchool(joinDto.getStudentId(), joinDto.getSchool()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다");
        }

        Member member = Member.builder()
                .studentId(joinDto.getStudentId())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .username(joinDto.getUsername())
                                .school(joinDto.getSchool())
                                        .department(joinDto.getDepartment())
                                                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public boolean isDuplicatedMember(MemberIdentifierDto memberIdentifierDto) {
        if(memberRepository.findByStudentIdAndSchool(memberIdentifierDto.getStudentId(), memberIdentifierDto.getSchool()).isPresent()) {
            return true;
        }
        return false;
    }


//    public Member findById(Long id) {
//        return memberRepository.findById(id);
//    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public List<Member> findFriendshipRequest(Long id) {
        return memberRepository.findFriendshipRequest(id);
    }

    public void withdrawal() {
         CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // redis에서 studentId + ":refreshToken" 키 삭제
        String studentId = customUserDetails.getStudentId();
        String school = customUserDetails.getSchool();
        Long id = customUserDetails.getId();
        log.info("탈퇴 요청 PK: {}", id);
        log.info("school = {}", school);
        log.info("studentId = {}", studentId);

        redisUtil.deleteKey(SchoolNameConverter.convertToEng(school) + ":" + studentId + ":refreshToken");
        memberRepository.deleteById(id);
    }

    // 친구 요청 보내기
    public void sendFriendRequest(RequestFriendshipDto requestFriendshipDto) {

        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member requester = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member receiver = memberRepository.findById(requestFriendshipDto.getReceiverId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        if(friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).isPresent()) {
            throw new DuplicateKeyException("이미 친구 요청된 상태입니다.");
        }

        Friendship friendship = Friendship.builder()
                .friendshipStatus(FriendshipStatus.PENDING)
                .requester(requester)
                .receiver(receiver)
                .build();

        friendship.sendFriendshipRequest(requester, receiver);

        friendshipRepository.saveFriendship(friendship);
    }

    // 친구요청 수락
    public void acceptFriendRequest(ReceiveFriendshipDto receiveFriendshipDto) {

        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member receiver = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(receiveFriendshipDto.getRequesterId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).orElseThrow(() -> new NoSuchElementException("해당 요청이 유효하지 않습니다."));

        if(friendship.getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
            throw new DuplicateKeyException("이미 수락된 요청입니다.");
        }

        friendshipRepository.acceptFriendRequest(friendship);
    }

    // 친구요청 거절
    public void rejectFriendRequest(Long requesterId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member receiver = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(requesterId).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Friendship friendship = friendshipRepository.findByRequesterAndReceiver(requester.getId(), receiver.getId()).orElseThrow(() -> new NoSuchElementException("해당 요청이 유효하지 않습니다."));

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
