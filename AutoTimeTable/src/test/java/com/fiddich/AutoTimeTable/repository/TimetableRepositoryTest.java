package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.entity.Timetable;
import com.fiddich.AutoTimeTable.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TimetableRepositoryTest {

    @Autowired
    TimetableRepository timetableRepository;
    @Autowired
    MemberService memberService;

    Member createMember() {
        Member member = Member.builder().build();
        return member;
    }

//    Timetable createTimetable() {
//        Timetable timetable = new Timetable(crea)
//    }



    @Test
    @Transactional
    @Rollback(value = false)
    void save() {
        Member member1 = createMember();
        memberService.save(member1);
        Member member2 = createMember();
        memberService.save(member2);

        Timetable timetable = new Timetable();
        timetable.setMember(member2);
        timetableRepository.save(timetable);

        Assertions.assertThat(member2.getId()).isEqualTo(timetable.getMember().getId());

    }

    @Test
    void findById() {
        Member member1 = createMember();
        memberService.save(member1);
        Member member2 = createMember();
        memberService.save(member2);
    }

    @Test
    void findAll() {
    }
}