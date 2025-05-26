package org.fiddich.api.domain.member.controller;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
import org.fiddich.api.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/join")
    public ApiResponse<Long> join(@RequestBody JoinDto joinDto) {
        log.info(joinDto.toString());
        return ApiResponse.onSuccess(memberService.join(joinDto));
    }

    @DeleteMapping("/withdrawal")
    public ApiResponse<Void> withdrawal() {
        memberService.withdrawal();
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/checkDuplicatedMember")
    public ApiResponse<Void> checkDuplicatedMember(@RequestBody MemberIdentifierDto memberIdentifierDto) {
        if(memberService.isDuplicatedMember(memberIdentifierDto)) {
            return ApiResponse.onFailure(HttpStatus.CONFLICT.name(), "이미 존재하는 회원입니다");
        }
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/friend/sendFriendRequest")
    public ApiResponse<Void> sendFriendRequest(@RequestBody RequestFriendshipDto requestFriendshipDto) {
        log.info("sendFriendRequest");
        memberService.sendFriendRequest(requestFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/friend/acceptFriendRequest")
    public ApiResponse<Void> acceptFriendRequest(@RequestBody ReceiveFriendshipDto receiveFriendshipDto) {
        log.info("acceptFriendRequest");
        memberService.acceptFriendRequest(receiveFriendshipDto);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/friend/rejectFriendRequest")
    public ApiResponse<Void> rejectFriendRequest(@RequestParam Long requesterId) {
        log.info("rejectFriendRequest");
        memberService.rejectFriendRequest(requesterId);
        return ApiResponse.onSuccess(null);
    }

//    @GetMapping("/friend/getMyFriends")
//    public ApiResponse<FriendDto[]> getMyFriends() {
//        log.info("getMyFriends");
//        FriendDto[] friendDtos = memberService.
//    }

}
