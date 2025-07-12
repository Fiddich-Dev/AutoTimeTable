package org.fiddich.api.domain.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.MemberService;
import org.fiddich.api.domain.member.dto.InquiryMemberDto;
import org.fiddich.api.domain.member.dto.FriendShipDto;
import org.fiddich.api.domain.member.dto.ReceiveFriendshipDto;
import org.fiddich.api.domain.member.dto.SearchMemberDto;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final MemberService memberService;

    @GetMapping("/friends")
    public ApiResponse<List<InquiryMemberDto>> getMyFriends() {
        log.info("친구 조회");
        return ApiResponse.onSuccess(memberService.findAllFriends());
    }

    @GetMapping("/friends/pending/responses")
    public ApiResponse<List<InquiryMemberDto>> findPendingResponse() {
        log.info("대기중인 응답 조회");
        return ApiResponse.onSuccess(memberService.findPendingResponse());
    }

    @GetMapping("/friends/pending/requests")
    public ApiResponse<List<InquiryMemberDto>> findPendingRequest() {
        log.info("대기중인 요청 조회");
        return ApiResponse.onSuccess(memberService.findPendingRequest());
    }

    @PostMapping("/friends/request")
    public ApiResponse<Void> sendFriendRequest(@RequestBody FriendShipDto requestFriendshipDto) {
        log.info("친구 요청");
        memberService.sendFriendRequest(requestFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/friends/accept")
    public ApiResponse<Void> acceptFriendRequest(@RequestBody FriendShipDto receiveFriendshipDto) {
        log.info("친구 요청 수락");
        memberService.acceptFriendRequest(receiveFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/request")
    public ApiResponse<Void> rejectFriendRequest(@RequestParam Long requesterId) {
        log.info("친구 요청 거절");
        memberService.rejectFriendRequest(requesterId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId) {
        log.info("친구 삭제");
        memberService.deleteFriend(friendId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/friends/search")
    public ApiResponse<List<SearchMemberDto>> searchMemberByStudentId(@RequestParam String keyword) {
        log.info("친구 검색");
        return ApiResponse.onSuccess(memberService.searchMemberByStudentId(keyword));
    }



}
