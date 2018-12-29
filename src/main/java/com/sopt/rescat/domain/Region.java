package com.sopt.rescat.domain;


import com.sopt.rescat.dto.RegionDto;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@ToString
@Getter
@Entity
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
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
                .name(String.format(sdName, " ", sggName, " ", emdName))
                .build();
    }

    public boolean equals(Region region) {
        return this.emdName.equals(region.emdName);
    }
}
