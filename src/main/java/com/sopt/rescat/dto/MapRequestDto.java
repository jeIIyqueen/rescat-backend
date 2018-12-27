package com.sopt.rescat.dto;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.Photo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MapRequestDto {
    private String name;
    private Float lat;
    private Float lng;
    private Integer radius;
    private Integer sex;
    private String age;
    private Integer tnr;
    private String etc;
    private Photo photo;
    private String region;

    @Builder
    public MapRequestDto(String name, Float lat, Float lng, Integer radius, Integer sex, String age, Integer tnr, String etc, Photo photo, String region) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.sex = sex;
        this.age = age;
        this.tnr = tnr;
        this.etc = etc;
        this.photo = photo;
        this.region = region;
    }

    public MapRequest toMapRequest(Long userIdx){


        return new MapRequest(name,etc,lat,lng,photo,radius,sex,age,tnr);
    }
}
