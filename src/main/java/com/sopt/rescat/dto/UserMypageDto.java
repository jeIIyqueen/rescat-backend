package com.sopt.rescat.dto;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserMypageDto {
    private String id;
    private String nickname;
    private String photoUrl;
    private Role role;
    private List<RegionDto> regions;

//    @Builder
//    public UserMypageDto(String id, String nickname, Role role){
//        this.id = id;
//        this.nickname = nickname;
//        this.role = role;
//    }

    public UserMypageDto(User user) {
    }
}
