package com.sopt.rescat.dto;

import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.Photo;
import com.sopt.rescat.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MapRequestDto {
    // 0: 등록, 1: 수정
    private Integer requestType;

    // 0: 고양이, 1: 배식소, 2: 병원
    private Integer registerType;

    private String name;

    private String etc;

    private Float lat;

    private Float lng;

    private Photo photo;

    // 아래는 고양이 고유
    private Integer radius;

    // 0: 남, 1: 여
    private Integer sex;

    private String age;

    // 0: 미완료, 1: 완료
    private Integer tnr;

    public MapRequest toMapRequest(User user){
        return MapRequest.builder()
                .writer(user)
                .isConfirmed(false)
                .requestType(requestType)
                .registerType(registerType)
                .name(name)
                .etc(etc)
                .lat(lat)
                .lng(lng)
                .photo(photo)
                .radius(radius)
                .sex(sex)
                .age(age)
                .tnr(tnr)
                .build();
    }
}
