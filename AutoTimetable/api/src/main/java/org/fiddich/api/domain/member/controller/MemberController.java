package org.fiddich.api.domain.member.controller;


import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.JoinDto;
import org.fiddich.api.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.common.ApiResponse;
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
    public ApiResponse<?> withdrawal() {
        memberService.withdrawal();
        return ApiResponse.onSuccess(null);
    }

}
