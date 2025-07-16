package org.fiddich.coreinfradomain.domain.Lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class School {

    @Id
    @Column(name = "school_id")
    private Long id;

    private String name;

    public School(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
