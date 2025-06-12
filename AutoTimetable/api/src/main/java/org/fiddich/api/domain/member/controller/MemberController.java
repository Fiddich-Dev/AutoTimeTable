package org.fiddich.api.domain.member.controller;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
import org.fiddich.api.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Member.SchoolNameConverter;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/join")
    public ApiResponse<Long> join(@RequestBody JoinDto joinDto) {
        log.info("join");
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

    @GetMapping("/friend/getMyFriends")
    public ApiResponse<List<FriendDto>> getMyFriends() {
        log.info("getMyFriends");
        List<FriendDto> friendDtos = memberService.findAllFriends();
        return ApiResponse.onSuccess(friendDtos);
    }

    @GetMapping("/friend/findPendingResponse")
    public ApiResponse<List<FriendDto>> findPendingResponse() {
        log.info("findPendingResponse");
        List<FriendDto> friendDtos = memberService.findPendingResponse();
        return ApiResponse.onSuccess(friendDtos);
    }

    @GetMapping("/friend/findPendingRequest")
    public ApiResponse<List<FriendDto>> findPendingRequest() {
        log.info("findPendingRequest");
        List<FriendDto> friendDtos = memberService.findPendingRequest();
        return ApiResponse.onSuccess(friendDtos);
    }

    @PostMapping("/auth/school")
    public ApiResponse<?> authSchool(@RequestBody AuthSchoolDto authSchoolDto) throws Exception {
        log.info("authSchool");
        AuthSchoolResponse authSchoolResponse = memberService.authSchool(authSchoolDto);
        // returncode, uid, username
        if(authSchoolResponse == null) {
            throw new NoSuchElementException("로그인 정보 없음");
        }
        if(authSchoolResponse.getReturnCode().equals("success")) {
            return ApiResponse.onSuccess(authSchoolResponse);
        }
        else {
            return ApiResponse.onFailure("123", "123");
        }
    }

    // 인증 메일은 보낸다
    // 인증이 되면 비밀번호 재설정 기회는 준다
    @PostMapping("/password-reset")
    public ApiResponse<?> passwordReset(@RequestBody ResetPasswordDto resetPasswordDto) {
        log.info("passwordReset()");
        memberService.resetPassword(resetPasswordDto.getSchool(), resetPasswordDto.getStudentId(), resetPasswordDto.getNewPassword());
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/friend/searchMemberByStudentId")
    public ApiResponse<FriendDto> searchMemberByStudentId(@RequestParam String school, @RequestParam String studentId) {
        log.info("searchMemberByStudentId()");
        FriendDto friendDto = memberService.searchMemberByStudentId(school, studentId);
        return ApiResponse.onSuccess(friendDto);
    }

    @DeleteMapping("/friend/deleteFriend")
    public ApiResponse<Void> deleteFriend(@RequestParam Long friendId) {
        log.info("deleteFriend()");
        memberService.deleteFriend(friendId);
        return ApiResponse.onSuccess(null);
    }

}
