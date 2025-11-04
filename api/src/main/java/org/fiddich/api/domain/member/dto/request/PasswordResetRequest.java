package org.fiddich.api.domain.member.dto.request;

public record PasswordResetRequest(
    String studentId,
    String newPassword
) {
}
