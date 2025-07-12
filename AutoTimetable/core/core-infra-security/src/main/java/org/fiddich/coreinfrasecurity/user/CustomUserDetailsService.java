package org.fiddich.coreinfrasecurity.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.coreinfradomain.domain.Member.Member;
import org.fiddich.coreinfradomain.domain.Member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 학번:학교 구조로 받기
    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {

        //DB에서 조회
        Member member = memberRepository.findByStudentId(studentId).orElseThrow(() -> new UsernameNotFoundException("해당회원 없음 " + studentId));

        log.info("user role = {}", member.getRole());

        return new CustomUserDetails(member);
    }


}
