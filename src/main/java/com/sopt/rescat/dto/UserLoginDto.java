package com.sopt.rescat.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserLoginDto {

    @ApiModelProperty(example = "ttmom96", position = 1)
    private String id;
    @ApiModelProperty(example = "ttmom1234", position = 2)
    private String password;

    @Builder
    public UserLoginDto(String id, String password) {
        this.id = id;
        this.password = password;
    }
}
