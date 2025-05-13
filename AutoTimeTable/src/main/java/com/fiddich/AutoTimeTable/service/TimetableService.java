package com.fiddich.AutoTimeTable.service;

import com.fiddich.AutoTimeTable.entity.Member;
import com.fiddich.AutoTimeTable.entity.Timetable;
import com.fiddich.AutoTimeTable.repository.MemberRepository;
import com.fiddich.AutoTimeTable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;

    public Long save(Long memberId, Timetable timetable) {
        Member member = memberRepository.findById(memberId);
        timetable.setMember(member);
        timetableRepository.save(timetable);
        return timetable.getId();
    }

    public Timetable findRepresentTimetable(Long memberId) {
        List<Timetable> timetables = findAllTimetable(memberId);
        // null이 나올수도 있음 고쳐야함
        return timetables.stream().filter(t -> t.getIsRepresent()).findFirst().get();
    }

    public List<Timetable> findAllTimetable(Long memberId) {
        return memberRepository.findById(memberId).getTimetables();
    }
}
