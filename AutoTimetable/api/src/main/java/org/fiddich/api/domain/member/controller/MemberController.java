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

    // 아마 안쓸듯
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
}
