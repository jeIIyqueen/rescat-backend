package jellyqueen.rescat.dto.response;

import jellyqueen.rescat.domain.enums.Role;
import jellyqueen.rescat.dto.JwtTokenDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserLoginResponseDto {
    JwtTokenDto jwtTokenDto;

    private Long idx;

    private List<String> regions;

    private List<Integer> emdCodes;

    private Long mileage;

    private Role role;
}
