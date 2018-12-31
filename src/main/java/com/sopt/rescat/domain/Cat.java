package com.sopt.rescat.domain;

import com.sopt.rescat.dto.CatDto;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
public class Cat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 10, nullable = false)
    private String name;

    @Column
    @NonNull
    private Float lat;

    @Column
    @NonNull
    private Float lng;

    @Column
    @NonNull
    private Integer radius;

    @Column
    @NonNull
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    private String age;

    @Column
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Column
    private String etc;

    @Column
    private String photoUrl;

    @OneToOne
    @NonNull
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_cat_region_idx"))
    private Region region;

    public CatDto toCatDto(){
        return CatDto.builder()
                .age(age).etc(etc).idx(idx).lat(lat).lng(lng)
                .name(name).photoUrl(photoUrl).radius(radius)
                .region(region.toRegionDto()).sex(sex).tnr(tnr)
                .build();
    }
}
