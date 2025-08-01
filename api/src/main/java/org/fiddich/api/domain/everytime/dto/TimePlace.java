package org.fiddich.api.domain.everytime.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TimePlace {
    String day;
    String start;
    String end;
    String place;
}
