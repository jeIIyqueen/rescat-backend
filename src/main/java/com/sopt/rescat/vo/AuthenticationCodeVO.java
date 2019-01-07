package com.sopt.rescat.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationCodeVO {
    @ApiModelProperty(example = "123456")
    private int code;
}
