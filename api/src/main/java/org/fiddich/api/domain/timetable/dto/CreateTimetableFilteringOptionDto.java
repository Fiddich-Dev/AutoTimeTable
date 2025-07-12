package org.fiddich.api.domain.timetable.dto;


import lombok.Data;

@Data
public class CreateTimetableFilteringOptionDto {
    int minCredit;
    int maxCredit;

    boolean preferMorning;
    boolean preferAfternoon;
    boolean minimizeSchoolDays;
    boolean minimizeEmptySlots;
}
