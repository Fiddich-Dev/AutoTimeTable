package org.fiddich.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinDto {

    private String studentId;
    private String password;
    private String username;

    @Override
    public String toString() {
        return "JoinDto{" +
                "studentId='" + studentId + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
