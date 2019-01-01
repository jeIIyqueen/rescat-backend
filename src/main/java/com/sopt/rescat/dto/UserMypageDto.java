package com.sopt.rescat.dto;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Role;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private Long mileage;

    public UserMypageDto(User user, List<RegionDto> regions){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.regions = regions;
        this.mileage = user.getMileage();
    }

    public UserMypageDto(User user){
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.phone = user.getPhone();
    }

}
