package org.fiddich.api.domain.friend.controller;

import lombok.RequiredArgsConstructor;
import org.fiddich.api.domain.friend.FriendService;
import org.fiddich.api.domain.friend.dto.response.MemberInfoResponse;
import org.fiddich.api.domain.friend.dto.request.FriendShipRequest;
import org.fiddich.api.domain.friend.dto.response.SearchMemberResponse;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/friends")
    public ApiResponse<List<MemberInfoResponse>> getMyFriends() {
        return ApiResponse.onSuccess(friendService.findAllFriends());
    }

    @GetMapping("/friends/pending/responses")
    public ApiResponse<List<MemberInfoResponse>> findPendingResponse() {
        return ApiResponse.onSuccess(friendService.findPendingResponse());
    }

    @PostMapping("/friends/request")
    public ApiResponse<Void> sendFriendRequest(@RequestBody FriendShipRequest requestFriendshipRequest) {
        friendService.sendFriendRequest(requestFriendshipRequest);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/friends/accept")
    public ApiResponse<Void> acceptFriendRequest(@RequestBody FriendShipRequest receiveFriendshipRequest) {
        friendService.acceptFriendRequest(receiveFriendshipRequest);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/request")
    public ApiResponse<Void> rejectFriendRequest(@RequestParam Long requesterId) {
        friendService.rejectFriendRequest(requesterId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friends/{friendId}")
    public ApiResponse<Void> deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/friends/search")
    public ApiResponse<List<SearchMemberResponse>> searchMemberByStudentId(@RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
        if (keyword == null || keyword.isEmpty()) {
            return ApiResponse.onFailure("400", "검색조건 오류");
        }
        return ApiResponse.onSuccess(friendService.searchMemberByStudentId(keyword, page, size));
    }

}
