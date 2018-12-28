package com.sopt.rescat.dto;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.Photo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CatDto {
    private Long idx;

    private String name;

    private Float lat;

    private Float lng;

    private Integer radius;

    // 0: 남, 1: 여
    private Integer sex;

    private LocalDateTime birth;

    // 0: 미완료, 1: 완료
    private Integer tnr;

    private String etc;

    private String photoUrl;

    private RegionDto region;

}
