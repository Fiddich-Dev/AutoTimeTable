package org.fiddich.api.domain.member;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EntityManager em;

    public Long join(JoinDto joinDto) {

//         학번이 안겹치는지 확인하는 로직
        if(memberRepository.findByStudentId(joinDto.getStudentId()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다");
        }

//        School school = memberRepository.findSchoolByName(joinDto.getSchool());

        Member member = Member.builder()
                .studentId(joinDto.getStudentId())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .username(joinDto.getUsername())
                                                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public boolean isDuplicatedMember(String studentId) {
        if(memberRepository.findByStudentId(studentId).isPresent()) {
            return true;
        }
        return false;
    }


    public void withdrawal() {
         CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // redis에서 studentId + ":refreshToken" 키 삭제
        String studentId = customUserDetails.getStudentId();
//        String school = customUserDetails.getSchool();
        Long id = customUserDetails.getId();
        log.info("탈퇴 요청 PK: {}", id);
//        log.info("school = {}", school);
        log.info("studentId = {}", studentId);

        redisUtil.deleteKey(studentId + ":refreshToken");




        memberRepository.deleteById(id);
    }

    // 친구 요청 보내기
    public void sendFriendRequest(FriendShipDto requestFriendshipDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member receiver = memberRepository.findById(requestFriendshipDto.getMemberId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        me.requestFriendship(receiver);
    }

    // 친구요청 수락
    public void acceptFriendRequest(FriendShipDto receiveFriendshipDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(receiveFriendshipDto.getMemberId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        me.acceptFriendship(requester);
    }

    // 친구요청 거절
    public void rejectFriendRequest(Long requesterId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member requester = memberRepository.findById(requesterId).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        me.rejectFriendship(requester);
    }

    // 친구 삭제
    public void deleteFriend(Long friendId) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Member friendToRemove = memberRepository.findById(friendId).orElseThrow(() -> new NoSuchElementException("해당 친구가 존재하지 않습니다."));
        me.removeFriend(friendToRemove);
    }

    // 내 친구들 조회
    public List<InquiryMemberDto> findAllFriends() {
        log.warn("연관관계 메서드 생각해보기");
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        return me.getFriends().stream().map(InquiryMemberDto::new).toList();
    }

    // 받은 요청중 보류중인거
    public List<InquiryMemberDto> findPendingResponse() {
        log.warn("연관관계 메서드 생각해보기");
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        return me.getPendingFriends().stream().map(InquiryMemberDto::new).toList();
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        Member me = memberRepository.findByStudentId(resetPasswordDto.getStudentId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.changePassword(encodedPassword);
    }

    public void changePassword(ResetPasswordDto resetPasswordDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.changePassword(encodedPassword);
    }

    public List<SearchMemberDto> searchMemberByStudentId(String keyword) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        List<Member> myFriends = me.getFriends();

        // 내가 요청 보냈거나, 받은 친구를 pending으로 표시
        List<Member> pendingFriends = new ArrayList<>();
        pendingFriends.addAll(friendshipRepository.findPendingRequests(me)); // 내가 받은 요청
        pendingFriends.addAll(friendshipRepository.findPendingRequest(me.getId())); // 내가 보낸 요청

        List<Member> members = memberRepository.findByStudentIdContaining(keyword);
        members.remove(me);

        return members.stream()
                .map(member -> {
                    SearchFriendStatus status;
                    if (myFriends.contains(member)) {
                        status = SearchFriendStatus.ALREADY_FRIEND;
                    } else if (pendingFriends.contains(member)) {
                        status = SearchFriendStatus.PENDING;
                    } else {
                        status = SearchFriendStatus.NOT_FRIEND;
                    }
                    return new SearchMemberDto(member, status);
                })
                .toList();
    }






}
