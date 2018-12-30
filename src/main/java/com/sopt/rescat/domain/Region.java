package com.sopt.rescat.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.dto.RegionDto;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@ToString
@Getter
@Entity
public class Region {
    @Id
    private Integer emdCode;

    @JsonIgnore
    @Column
    private Integer sdCode;

    @JsonIgnore
    @Column
    private String sdName;

    @JsonIgnore
    @Column
    private Integer sggCode;

    @JsonIgnore
    @Column
    private String sggName;

    @Column
    private String emdName;

    public RegionDto toRegionDto() {
        return RegionDto.builder().code(this.emdCode).name(sdName + " " + sggName + " " + emdName).build();
    }

    public RegionDto toRegionDto(Integer code, String name){
        return RegionDto.builder().code(code).name(name).build();
    }

    public boolean equals(Region region) {
        return this.emdName.equals(region.emdName);
    }
}