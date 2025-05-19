package com.fiddich.AutoTimeTable.repository;

import com.fiddich.AutoTimeTable.entity.Department;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LectureRepositoryTest {

    @Autowired
    LectureRepository lectureRepository;

    @Test
    void findByDepartment() {

        int cnt = lectureRepository.findByDepartment(Department.culture).size();
        System.out.println(cnt);
        Assertions.assertThat(cnt).isEqualTo(922);

    }
}