package org.fiddich.api.domain.friend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.friend.FriendService;
import org.fiddich.api.domain.friend.dto.InquiryMemberDto;
import org.fiddich.api.domain.friend.dto.FriendShipDto;
import org.fiddich.api.domain.friend.dto.SearchMemberDto;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/friends")
    public ApiResponse<List<InquiryMemberDto>> getMyFriends() {
        log.info("친구 조회");
        return ApiResponse.onSuccess(friendService.findAllFriends());
    }

    @GetMapping("/friends/pending/responses")
    public ApiResponse<List<InquiryMemberDto>> findPendingResponse() {
        log.info("대기중인 응답 조회");
        return ApiResponse.onSuccess(friendService.findPendingResponse());
    }

    @PostMapping("/friends/request")
    public ApiResponse<Void> sendFriendRequest(@RequestBody FriendShipDto requestFriendshipDto) {
        log.info("친구 요청");
        friendService.sendFriendRequest(requestFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/friends/accept")
    public ApiResponse<Void> acceptFriendRequest(@RequestBody FriendShipDto receiveFriendshipDto) {
        log.info("친구 요청 수락");
        friendService.acceptFriendRequest(receiveFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/request")
    public ApiResponse<Void> rejectFriendRequest(@RequestParam Long requesterId) {
        log.info("친구 요청 거절");
        friendService.rejectFriendRequest(requesterId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId) {
        log.info("친구 삭제");
        friendService.deleteFriend(friendId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/friends/search")
    public ApiResponse<List<SearchMemberDto>> searchMemberByStudentId(@RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
        log.info("친구 검색");
        return ApiResponse.onSuccess(friendService.searchMemberByStudentId(keyword, page, size));
    }

}
