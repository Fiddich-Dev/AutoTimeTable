package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Friendship;
import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.service.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    Member createMember() {
        Member member = Member.builder().build();
        return member;
    }

    @Test
    @DisplayName("")
    void save() throws Exception {
        // given
        Member member = createMember();
        // when
        Long memberId = memberService.save(member);
        // then
        assertThat(memberId).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("찾기")
    void find() throws Exception {
        // given
        Member member1 = createMember();
        Member member2 = createMember();
        Member member3 = createMember();
        // when
        Long memberId1 = memberService.save(member1);
        Long memberId2 = memberService.save(member2);
        Long memberId3 = memberService.save(member3);
        // then
        assertThat(member2.getId()).isEqualTo(memberService.findById(memberId2).getId());

        assertThat(memberService.findAll().size()).isEqualTo(3);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("")
    void findFriend() throws Exception {
        // given
        Member member1 = Member.builder()
                .name("A")
                .build();
        Member member2 = Member.builder()
                .name("B")
                .build();
        Member member3 = Member.builder()
                .name("C")
                .build();
        Member member4 = Member.builder()
                .name("D")
                .build();
        Member member5 = Member.builder()
                .name("E")
                .build();
        Member member6 = Member.builder()
                .name("F")
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);
        memberRepository.save(member6);

        memberService.saveFriendship(member1.getId(), member6.getId());
        memberService.saveFriendship(member2.getId(), member6.getId());
        memberService.saveFriendship(member3.getId(), member6.getId());

        // when
//        List<Member> members = memberService.findFriendshipRequest(member6.getId());
//        for(Member member : members) {
//            System.out.println(member.getName());
//        }

        // then
    }

}