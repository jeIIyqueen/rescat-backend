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
@NoArgsConstructor
@Builder
public class UserMypageDto {
    private String id;
    private String nickname;
    private String photoUrl;
    private Role role;
    private List<RegionDto> regions;

    public UserMypageDto (User user, List<RegionDto> regions) {
        UserMypageDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole())
                .regions(regions)
                .build();
    }
}
