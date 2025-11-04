package org.fiddich.api.domain.member.controller;


import org.fiddich.api.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.fiddich.api.domain.member.dto.request.PasswordRequest;
import org.fiddich.api.domain.member.dto.request.PasswordResetRequest;
import org.fiddich.api.domain.member.dto.request.SignUpRequest;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    public ApiResponse<Long> join(@RequestBody SignUpRequest signUpRequest) {
        return ApiResponse.onSuccess(memberService.join(signUpRequest));
    }

    @GetMapping("/members/check-duplicate")
    public ApiResponse<Void> checkDuplicatedMember(@RequestParam String studentId) {
        if(memberService.isDuplicatedMember(studentId)) {
            return ApiResponse.onFailure(HttpStatus.CONFLICT.name(), "이미 존재하는 회원입니다");
        }
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/members/me")
    public ApiResponse<Void> withdrawal() {
        memberService.withdrawal();
        return ApiResponse.onSuccess(null);
    }

    // 인증 메일은 보낸다
    // 인증이 되면 비밀번호 재설정 기회는 준다
    @PatchMapping("/password-reset")
    public ApiResponse<?> passwordReset(@RequestBody PasswordResetRequest passwordResetRequest) {
        memberService.resetPassword(passwordResetRequest);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/password-valid")
    public ApiResponse<Void> validPassword(@RequestBody PasswordRequest passwordRequest) {
        memberService.validPassword(passwordRequest);
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/password-change")
    public ApiResponse<Void> changePassword(@RequestBody PasswordRequest passwordRequest) {
        memberService.changePassword(passwordRequest);
        return ApiResponse.onSuccess(null);
    }

}
