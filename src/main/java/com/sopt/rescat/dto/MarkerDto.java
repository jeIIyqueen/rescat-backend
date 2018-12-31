package com.sopt.rescat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MarkerDto {
    private Long idx;

    private String name;

    private Double lat;

    private Double lng;

    private Integer radius;

    // 0: 남, 1: 여
    private Integer sex;

    private String age;

    // 0: 미완료, 1: 완료
    private Integer tnr;

    private String etc;

    private String photoUrl;

    private RegionDto region;

    // 0: 배식소, 1: 병원, 2:고양이
    private Integer category;

    private String address;

    private Integer phone;

}
