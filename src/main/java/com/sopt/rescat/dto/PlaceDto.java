package com.sopt.rescat.dto;

import com.sopt.rescat.domain.Photo;
import com.sopt.rescat.domain.Place;
import lombok.Builder;

@Builder
public class PlaceDto {
    private Long idx;

    // 0: 배식소, 1: 병원
    private Integer category;

    private String name;

    private Float lat;

    private Float lng;

    private String etc;

    private String address;

    private Integer phone;

    private String photoUrl;

    private RegionDto region;
}
