package com.sopt.rescat.dto;

import com.sopt.rescat.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserMypageDto {
    private String id;
    private String name;
    private String nickname;
    private Role role;
    private List<RegionDto> regions;
    private String phone;
    private Long mileage;
}
