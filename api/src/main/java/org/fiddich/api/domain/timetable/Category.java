package org.fiddich.api.domain.timetable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Category {
    String id;
    String name;
    String order;
    String parentId;
}