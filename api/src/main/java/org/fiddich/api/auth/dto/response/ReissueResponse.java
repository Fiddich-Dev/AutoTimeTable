package org.fiddich.api.auth.dto.response;

import lombok.Builder;
import org.fiddich.coreinfrasecurity.jwt.dto.JWTDto;

@Builder
public record ReissueResponse(
        String access,
        String refresh
) {

    public static ReissueResponse from(JWTDto jwtDto) {
        return ReissueResponse.builder()
                .access(jwtDto.getAccess())
                .refresh(jwtDto.getRefresh())
                .build();
    }
}
