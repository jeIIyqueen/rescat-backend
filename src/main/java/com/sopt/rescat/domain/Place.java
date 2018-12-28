package com.sopt.rescat.domain;

import com.sopt.rescat.dto.PlaceDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@ToString
@Getter
@Entity
@NoArgsConstructor
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    // 0: 배식소, 1: 병원
    private Integer category;

    @Column
    @NonNull
    @Length(max = 50)
    private String name;

    @Column
    @NonNull
    private Float lat;

    @Column
    @NonNull
    private Float lng;

    @Column
    private String etc;

    @Column
    @NonNull
    private String address;

    @Column
    @Length(max = 11)
    private Integer phone;

    @OneToOne
    private Photo photo;

    @OneToOne
    @NonNull
    private Region region;

    public PlaceDto toPlaceDto(){
        return PlaceDto.builder().address(this.address).category(this.category)
                .etc(this.etc).idx(this.idx).lat(this.lat).lng(this.lng).name(this.name)
                .phone(this.phone).photoUrl(this.photo.getUrl()).region(this.region.toRegionDto()).build();
    }
}
