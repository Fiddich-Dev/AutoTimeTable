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

    @PostMapping("/members")
    public ApiResponse<Long> join(@RequestBody JoinDto joinDto) {
        log.info("회원가입");
        return ApiResponse.onSuccess(memberService.join(joinDto));
    }

    @GetMapping("/members/check-duplicate")
    public ApiResponse<Void> checkDuplicatedMember(@RequestParam String studentId) {
        log.info("학번 중복체크");
        if(memberService.isDuplicatedMember(studentId)) {
            return ApiResponse.onFailure(HttpStatus.CONFLICT.name(), "이미 존재하는 회원입니다");
        }
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/members/me")
    public ApiResponse<Void> withdrawal() {
        log.info("회원탈퇴");
        memberService.withdrawal();
        return ApiResponse.onSuccess(null);
    }

    // 인증 메일은 보낸다
    // 인증이 되면 비밀번호 재설정 기회는 준다
    @PatchMapping("/password-reset")
    public ApiResponse<?> passwordReset(@RequestBody ResetPasswordDto resetPasswordDto) {
        log.info("passwordReset()");
        memberService.resetPassword(resetPasswordDto);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/password-valid")
    public ApiResponse<Void> validPassword(@RequestBody PasswordDto passwordDto) {
        log.info("password-valid");
        memberService.validPassword(passwordDto);
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/password-change")
    public ApiResponse<Void> changePassword(@RequestBody PasswordDto passwordDto) {
        log.info("password-change");
        memberService.changePassword(passwordDto);
        return ApiResponse.onSuccess(null);
    }

}
