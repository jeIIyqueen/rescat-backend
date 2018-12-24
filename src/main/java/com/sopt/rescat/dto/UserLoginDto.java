package com.sopt.rescat.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserLoginDto {
    private String id;
    private String password;

    @Builder
    public UserLoginDto(String id, String password) {
        this.id = id;
        this.password = password;
    }
}
