package org.fiddich.api.domain.lecture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiddich.api.domain.lecture.dto.InquiryDepartmentDto;
import org.fiddich.api.domain.timetable.dto.InternalLectureDto;
import org.fiddich.coreinfradomain.domain.Lecture.repository.LectureRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    // 커스텀 강의는 안나와야함
    public List<InternalLectureDto> searchLecturesByKeyword(String keyword, int page, int size) {
        if(keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }
        return lectureRepository.searchByAutoField(keyword, page, size).stream()
                .map(InternalLectureDto::new)
                .toList();
    }

    public List<InquiryDepartmentDto> getAllCategories(String year, String semester) {
        return lectureRepository.findAllCategoryByYearAndSemester(year, semester)
                .stream()
                .map(c -> new InquiryDepartmentDto(c.getId(), c.getName()))
                .toList();
    }

    public List<InquiryDepartmentDto> searchCategories(String keyword, String year, String semester, int page, int size) {
        return lectureRepository.searchCategoryByYearAndSemester(keyword, year, semester, page, size)
                .stream()
                .map(c -> new InquiryDepartmentDto(c.getId(), c.getName()))
                .toList();
    }
}
