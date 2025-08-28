package org.fiddich.api.domain.member;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.member.dto.*;
import org.fiddich.api.domain.timetable.TimetableService;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.fiddich.coreinfradomain.domain.Timetable.Timetable;
import org.fiddich.coreinfradomain.domain.Timetable.repository.TimetableRepository;
import org.fiddich.coreinfraredis.util.RedisUtil;
import org.fiddich.coreinfrasecurity.user.CustomUserDetails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TimetableRepository timetableRepository;

    public Long join(JoinDto joinDto) {
//         학번이 안겹치는지 확인하는 로직
        if(memberRepository.existsByStudentId(joinDto.getStudentId())) {
            throw new DuplicateKeyException("이미 존재하는 회원입니다");
        }

        Member member = Member.builder()
                .studentId(joinDto.getStudentId())
                .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .username(joinDto.getUsername())
                                                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public boolean isDuplicatedMember(String studentId) {
        return memberRepository.existsByStudentId(studentId);
    }

    public void withdrawal() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String studentId = customUserDetails.getStudentId();
        Long id = customUserDetails.getId();

        List<Timetable> allTimetables = timetableRepository.findAllByMemberId(customUserDetails.getId());
        for(Timetable timetable : allTimetables) {
            timetableRepository.deleteById(timetable.getId());
        }
        memberRepository.deleteById(id);
        // redis에서 studentId + ":refreshToken" 키 삭제
        redisUtil.deleteKey(studentId + ":refreshToken");
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        Member me = memberRepository.findByStudentId(resetPasswordDto.getStudentId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword());
        me.changePassword(encodedPassword);
    }

    public void validPassword(PasswordDto passwordDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String nowPassword = passwordDto.getPassword();
        boolean isMatch = bCryptPasswordEncoder.matches(nowPassword, me.getPassword());
        if (!isMatch) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
    }

    public void changePassword(PasswordDto passwordDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member me = memberRepository.findById(customUserDetails.getId()).orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        String newPassword = passwordDto.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);
        me.changePassword(encodedPassword);
    }

}
