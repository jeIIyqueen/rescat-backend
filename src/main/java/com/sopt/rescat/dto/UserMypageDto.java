package com.sopt.rescat.dto;

import com.sopt.rescat.domain.Photo;
import com.sopt.rescat.domain.enums.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserMypageDto {
    private String name;
    private String nickname;
    private Photo photo;
    private Role role;
    private List<RegionDto> regions;
}
