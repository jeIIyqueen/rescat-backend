package com.sopt.rescat.dto;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.Photo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private LocalDateTime birth;
    private Integer tnr;
    private String etc;
    private Photo photo;
    private String region;

    public MapRequestDto(String name, Float lat, Float lng, Integer radius, Integer sex, LocalDateTime birth, Integer tnr, String etc, Photo photo, String region) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.sex = sex;
        this.birth = birth;
        this.tnr = tnr;
        this.etc = etc;
        this.photo = photo;
        this.region = region;
    }

}
