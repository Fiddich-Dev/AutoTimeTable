package org.fiddich.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPasswordDto {
    private String studentId;
    private String newPassword;
}
