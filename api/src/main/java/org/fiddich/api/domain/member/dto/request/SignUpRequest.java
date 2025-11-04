package org.fiddich.api.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;


public record SignUpRequest(
    String studentId,
    String password,
    String username
) {
}
