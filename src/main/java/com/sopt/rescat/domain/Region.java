package com.sopt.rescat.domain;


import com.sopt.rescat.dto.RegionDto;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@ToString
@Getter
@Entity
public class Region {
    @Id
    private Integer emdCode;

    @Column
    private Integer sdCode;

    @Column
    private String sdName;

    @Column
    private Integer sggCode;

    @Column
    private String sggName;

    @Column
    private String emdName;

    public RegionDto toRegionDto() {
        return RegionDto.builder()
                .code(this.emdCode)
                .name(sdName + " " + sggName + " " + emdName)
                .build();
    }

    public boolean equals(Region region) {
        return this.emdName.equals(region.emdName);
    }
}