package org.fiddich.api.domain.member.dto;

import lombok.Getter;

@Getter
public class ResetPasswordDto {
    private String studentId;
    private String newPassword;
}
