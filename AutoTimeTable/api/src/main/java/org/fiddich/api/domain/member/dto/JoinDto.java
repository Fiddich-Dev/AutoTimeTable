package org.fiddich.api.domain.member.dto;

import lombok.Getter;

@Getter
public class JoinDto {

    private String studentId;
    private String password;
    private String username;
    private String school;
    private String department;

    @Override
    public String toString() {
        return "JoinDto{" +
                "studentId='" + studentId + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", school='" + school + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
