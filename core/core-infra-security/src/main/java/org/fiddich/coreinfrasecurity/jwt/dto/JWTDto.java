package org.fiddich.coreinfrasecurity.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JWTDto {
    private String access;
    private String refresh;
}
