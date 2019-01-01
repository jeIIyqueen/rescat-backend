package com.sopt.rescat.dto;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class UserMypageDto {
    private String id;
    private String nickname;
    private Role role;
    private List<RegionDto> regions;
    private String phone;

    public UserMypageDto(User user, List<RegionDto> regions) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.regions = regions;
    }

//    public UserMypageDto(User user){
//        this.id = user.getId();
//        this.nickname = user.getNickname();
//        this.role = user.getRole();
//        this.phone = user.getPhone();
//    }

}
