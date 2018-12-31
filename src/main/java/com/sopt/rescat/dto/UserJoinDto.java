package com.sopt.rescat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.exception.NotMatchException;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Pattern;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserJoinDto {
    private final static String ID_REGEX = "^[a-z]+[a-z0-9]{5,19}$";
    private final static String PASSWORD_REGEX = "^(?=.*?[a-zA-Z])(?=.*?[0-9]).{8,12}$";
    private final static String NICKNAME_REGEX = "^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{2,20}$";

    @ApiModelProperty(example = "ttmom96", position = 1)
    @Pattern(regexp = ID_REGEX, message = "아이디는 영문자로 시작하는 6~20자 영문자 또는 숫자이어야 합니다.")
    private String id;

    @ApiModelProperty(example = "ttmom1234", position = 2)
    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영문자, 숫자 8~12자이어야 합니다.")
    private String password;

    @ApiModelProperty(example = "ttmom1234", position = 3)
    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영문자, 숫자 8~12자이어야 합니다.")
    private String rePassword;

    @ApiModelProperty(example = "티티엄마", position = 4)
    @Pattern(regexp = NICKNAME_REGEX, message = "닉네임은 특수문자 제외 2~20자이어야 합니다.")
    private String nickname;

    public User toUser(String encodedPassword) throws NotMatchException {
        if (!matchPassword()) throw new NotMatchException("password", "비밀번호가 일치하지 않습니다.");
        return new User(id, encodedPassword, nickname);
    }

    private boolean matchPassword() {
        return rePassword.equals(password);
    }
}
