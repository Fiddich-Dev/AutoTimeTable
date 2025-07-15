package org.fiddich.api.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Long join(JoinDto joinDto) {

//         학번이 안겹치는지 확인하는 로직
        if(memberRepository.findByStudentId(joinDto.getStudentId()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다");
        }

//        School school = memberRepository.findSchoolByName(joinDto.getSchool());

        Member member = Member.builder()
                .studentId(joinDto.getStudentId())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .username(joinDto.getUsername())
                                                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public boolean isDuplicatedMember(String studentId) {
        if(memberRepository.findByStudentId(studentId).isPresent()) {
            return true;
        }
        return false;
    }


    public void withdrawal() {
         CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // redis에서 studentId + ":refreshToken" 키 삭제
        String studentId = customUserDetails.getStudentId();
//        String school = customUserDetails.getSchool();
        Long id = customUserDetails.getId();
        log.info("탈퇴 요청 PK: {}", id);
//        log.info("school = {}", school);
        log.info("studentId = {}", studentId);

        redisUtil.deleteKey(studentId + ":refreshToken");




        memberRepository.deleteById(id);
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        Member me = memberRepository.findByStudentId(resetPasswordDto.getStudentId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.changePassword(encodedPassword);
    }

    public void changePassword(ResetPasswordDto resetPasswordDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.changePassword(encodedPassword);
    }

}
