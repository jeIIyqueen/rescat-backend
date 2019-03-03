package jellyqueen.rescat.dto;

import jellyqueen.rescat.exception.NotMatchException;
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
public class UserPasswordDto {
    private final static String PASSWORD_REGEX = "^(?=.*?[a-zA-Z])(?=.*?[0-9]).{8,12}$";

    @ApiModelProperty(example = "ttmom1234", position = 1)
    private String password;

    @ApiModelProperty(example = "ttmom5678", position = 2)
    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영문자, 숫자 8~12자이어야 합니다.")
    private String newPassword;

    @ApiModelProperty(example = "ttmom5678", position = 3)
    @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 영문자, 숫자 8~12자이어야 합니다.")
    private String reNewPassword;

    public boolean checkValidPassword() throws NotMatchException {
        if (!reNewPassword.equals(newPassword)) throw new NotMatchException("rePassword", "비밀번호가 일치하지 않습니다.");
        return true;
    }
}
