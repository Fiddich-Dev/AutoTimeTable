package org.fiddich.api.domain.friend;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.friend.dto.FriendShipDto;
import org.fiddich.api.domain.friend.dto.InquiryMemberDto;
import org.fiddich.api.domain.friend.dto.SearchFriendStatus;
import org.fiddich.api.domain.friend.dto.SearchMemberDto;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.fiddich.coreinfradomain.domain.friendship.Friendship;
import org.fiddich.coreinfradomain.domain.friendship.FriendshipStatus;
import org.fiddich.coreinfradomain.domain.friendship.repository.FriendshipRepository;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
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
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        return me.getFriends().stream().map(InquiryMemberDto::new).toList();
    }

    // 받은 요청중 보류중인거
    public List<InquiryMemberDto> findPendingResponse() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        return me.getPendingFriends().stream().map(InquiryMemberDto::new).toList();
    }
    
    // 친구 검색
    public List<SearchMemberDto> searchMemberByStudentId(String keyword, int page, int size) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        // 나와 관련된 모든 관계 가져오기
        List<Friendship> allMyFriendShips = friendshipRepository.findAllByMemberId(me.getId());

        // 검색
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "studentId"));
        Page<Member> memberPage = memberRepository.findByStudentIdContaining(keyword, pageRequest);
        List<Member> allMembers = memberPage.getContent();

        Map<Long, FriendshipStatus> friendStatusMap = allMyFriendShips.stream()
                .collect(Collectors.toMap(
                        fs -> fs.getRequester().getId().equals(me.getId()) ? fs.getReceiver().getId() : fs.getRequester().getId(),
                        Friendship::getFriendshipStatus,
                        (status1, status2) -> status1 // 중복 시 하나만
                ));

        return allMembers.stream()
                .filter(member -> !member.getId().equals(me.getId()))
                .map(member -> {
                    FriendshipStatus fs = friendStatusMap.get(member.getId());
                    SearchFriendStatus status = (fs == FriendshipStatus.ACCEPTED) ? SearchFriendStatus.ALREADY_FRIEND
                            : (fs == FriendshipStatus.PENDING) ? SearchFriendStatus.PENDING
                            : SearchFriendStatus.NOT_FRIEND;
                    return new SearchMemberDto(member, status);
                })
                .toList();
    }

    // 친구 검색 이전버전
    //    public List<SearchMemberDto> searchMemberByStudentId(String keyword, int page, int size) {
//        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
//
//        // 1. 친구들의 ID를 Set으로 미리 추출합니다. (조회 성능 O(1))
//        Set<Long> myFriendIds = me.getFriends().stream()
//                .map(Member::getId)
//                .collect(Collectors.toSet());
//
//        // 2. 요청을 보내거나 받은 친구들의 ID를 Set으로 미리 추출합니다.
//        List<Member> pendingMembers = new ArrayList<>();
//        pendingMembers.addAll(friendshipRepository.findPendingRequestsReceivedBy(me)); // 내가 받은 요청
//        pendingMembers.addAll(friendshipRepository.findPendingRequestsSentBy(me.getId())); // 내가 보낸 요청
//
//        Set<Long> pendingFriendIds = pendingMembers.stream()
//                .map(Member::getId)
//                .collect(Collectors.toSet());
//
//        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "studentId"));
//
//        // 3. 학번으로 멤버를 검색합니다.
//        Page<Member> memberPage = memberRepository.findByStudentIdContaining(keyword, pageRequest);
//        List<Member> allMembers = memberPage.getContent();
//
//        // 4. 검색된 멤버 리스트를 스트림으로 변환하여 DTO로 만듭니다.
//        return allMembers.stream()
//                // 나 자신은 검색 결과에서 제외합니다.
//                .filter(member -> !member.getId().equals(me.getId()))
//                // DTO로 변환하며 친구 상태를 설정합니다.
//                .map(member -> {
//                    SearchFriendStatus status;
//                    // Member 객체가 아닌 ID로 포함 여부를 확인합니다.
//                    if (myFriendIds.contains(member.getId())) {
//                        status = SearchFriendStatus.ALREADY_FRIEND;
//                    } else if (pendingFriendIds.contains(member.getId())) {
//                        status = SearchFriendStatus.PENDING;
//                    } else {
//                        status = SearchFriendStatus.NOT_FRIEND;
//                    }
//                    return new SearchMemberDto(member, status);
//                })
//                .toList();
//    }
}
