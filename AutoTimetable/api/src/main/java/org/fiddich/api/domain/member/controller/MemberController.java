package org.fiddich.api.domain.member.controller;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.JoinDto;
import org.fiddich.api.domain.member.MemberIdentifierDto;
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

}
