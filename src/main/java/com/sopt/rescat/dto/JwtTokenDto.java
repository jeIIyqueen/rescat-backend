package com.sopt.rescat.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class JwtTokenDto {
    private String token;

    @Builder
    public JwtTokenDto(String token) {
        this.token = token;
    }
}
