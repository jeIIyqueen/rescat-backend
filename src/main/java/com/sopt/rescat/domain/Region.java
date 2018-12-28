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
    @Column
    private Integer sdcode;

    @Column
    private String sdname;

    @Column
    private Integer sggcode;

    @Column
    private String sggname;

    @Column
    @Id
    private Integer emdcode;

    @Column
    private String emdname;

    public RegionDto toRegionDto() {
        return RegionDto.builder().code(this.emdcode).name(sdname + " " + sggname + " " + emdname).build();
    }

    public boolean equals(Region region) {
        return this.emdname == region.emdname;
    }
}
