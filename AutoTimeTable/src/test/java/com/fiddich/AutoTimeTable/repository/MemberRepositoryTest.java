package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Friendship;
import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.service.MemberService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberService memberService;
    @Autowired
    EntityManager em;

    void createAndSendMember() {
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

        memberService.save(member1);
        memberService.save(member2);
        memberService.save(member3);
        memberService.save(member4);
        memberService.save(member5);
        memberService.save(member6);

        memberService.sendFriendRequest(member2, member6);
        memberService.sendFriendRequest(member4, member6);
        memberService.sendFriendRequest(member5, member6);
    }

    @Test
    @DisplayName("")
    void save() throws Exception {
        // given
        Member member = Member.builder().build();
        // when
        Long memberId = memberService.save(member);
        // then
        assertThat(memberId).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("찾기")
    void find() throws Exception {
        // given
        Member member1 = Member.builder().build();
        Member member2 = Member.builder().build();
        Member member3 = Member.builder().build();
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
    @DisplayName("친구 요청 보내기")
    void sendFriendRequest() throws Exception {
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

        // when
        memberService.save(member1);
        memberService.save(member2);
        memberService.save(member3);
        memberService.save(member4);
        memberService.save(member5);
        memberService.save(member6);

        memberService.sendFriendRequest(member2, member6);
        memberService.sendFriendRequest(member4, member6);
        memberService.sendFriendRequest(member5, member6);

        // then
        assertThat(memberService.findAll().size()).isEqualTo(6);
        assertThat(memberService.findPendingRequests(member6).size()).isEqualTo(3);
        assertThat(memberService.findAcceptedRequests(member6).size()).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("친구 요청 수락, 거절")
    void acceptAndRejectFriendshipRequest() throws Exception {
        // given
        createAndSendMember();
        Member member6 = memberService.findById(6L);
        Member member2 = memberService.findById(2L);
        Member member4 = memberService.findById(4L);

        // when
        memberService.acceptFriendRequest(member2, member6);
        memberService.acceptFriendRequest(member4, member6);

        em.flush();
        em.clear();


        for(Friendship friendship : member6.getReceivedFriendships()) {
            log.info("memberName = {}, friendshipStatus = {}", friendship.getRequester().getName(), String.valueOf(friendship.getFriendshipStatus()));
        }


        // then

        assertThat(member6.getFriends().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("내가 보낸 친구요청 확인")
    void MemberRepositoryTest() throws Exception {
        // given
        createAndSendMember();
        Member member2 = memberService.findById(2L);
        // when
        em.flush();
        em.clear();

        // then
        List<Member> receivers = memberService.findPendingMyRequest(member2);
        for(Member member : receivers) {
            log.info("name = {}", member.getName());
        }
    }


}