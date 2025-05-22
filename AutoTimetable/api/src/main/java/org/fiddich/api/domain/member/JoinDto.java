package org.fiddich.api.domain.member;

import lombok.Getter;

@Getter
public class JoinDto {

    private String studentId;
    private String password;
    private String name;
    private String school;
    private String department;

}
