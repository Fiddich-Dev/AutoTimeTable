package org.fiddich.api.domain.lecture;


import org.fiddich.coreinfradomain.domain.Lecture.Lecture;
import org.fiddich.coreinfradomain.domain.Lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;


    private void combi(List<Lecture> cultures, List<Lecture> majors, int start, List<List<Lecture>> ret, List<Lecture> current) {
        if(current.size() == 3) {
            ret.add(current);
            return;
        }
        for(int i = start+1; i < majors.size(); i++) {
            current.add(majors.get(i));
            combi(cultures, majors, i, ret, current);
            current.remove(current.size() - 1);
        }
    }

//    // 꼭들어야하는 강의는 미리 넣어 놓기
//    public List<List<Lecture>> createTimetable() {
//
//        // 교양 강의 가져오기
//        List<Lecture> cultures = lectureRepository.findByDepartment(Department.culture);
//        // 전공 강의들 가져오기
//        List<Lecture> majors = lectureRepository.findByDepartment(Department.classicalChinese);
//
//        // 전공중에 3개 뽑기
//        List<List<Lecture>> ret = new ArrayList<>();
//        combi(cultures, majors, -1, ret, new ArrayList<>());
//
//        System.out.println(ret.size());
//
//        return ret;
//    }

//     code가 안겹치는지
//     시간이 안겹치는지
//    private boolean isValid() {
//
//    }
//
//    // 사용자가 정한 기준에 부합하는지
//    // 안듣고 싶은 과목은 없는지
//    // 전공, 교양 학점은 맞는지
//    private boolean check() {
//
//    }



}
