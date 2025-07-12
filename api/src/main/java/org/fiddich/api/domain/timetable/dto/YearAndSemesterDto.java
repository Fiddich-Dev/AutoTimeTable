package org.fiddich.api.domain.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class YearAndSemesterDto {

    private String year;
    private String semester;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        YearAndSemesterDto that = (YearAndSemesterDto) o;
        return Objects.equals(year, that.year) && Objects.equals(semester, that.semester);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, semester);
    }
}
