package org.fiddich.api.domain.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.friend.dto.FriendShipDto;
import org.fiddich.api.domain.friend.dto.InquiryMemberDto;
import org.fiddich.api.domain.friend.dto.SearchFriendStatus;
import org.fiddich.api.domain.friend.dto.SearchMemberDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.friendship.repository.FriendshipRepository;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

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

    public List<SearchMemberDto> searchMemberByStudentId(String keyword, int page, int size) {
        if(keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        List<Member> myFriends = me.getFriends();

        List<Member> allMembers = memberRepository.findByStudentIdContaining(keyword, page, size);
        allMembers.remove(me);

        // 내가 요청 보냈거나, 받은 친구를 pending으로 표시
        List<Member> pendingFriends = new ArrayList<>();
        pendingFriends.addAll(friendshipRepository.findPendingRequests(me)); // 내가 받은 요청
        pendingFriends.addAll(friendshipRepository.findPendingRequest(me.getId())); // 내가 보낸 요청

//        List<Member> members = memberRepository.findByStudentIdContaining(keyword, page, size);
//        members.remove(me);

        return allMembers.stream()
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
