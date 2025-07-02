package com.fiddich.LectureMapping;

import com.fiddich.LectureMapping.entity.School;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "schools")
@Getter
@Setter
public class SchoolsProperties {

    private List<School> schools;

    @Getter
    @Setter
    public static class School {
        private String name;
        private String code;
        private List<Department> departments;

        @Getter
        @Setter
        public static class Department {
            private String name;
            private String code;
        }
    }
}


