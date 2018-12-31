package com.sopt.rescat.domain;

import com.sopt.rescat.dto.MarkerDto;
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
    private Double lat;

    @Column
    @NonNull
    private Double lng;

    @Column
    private String etc;

    @Column
    @NonNull
    private String address;

    @Column
    @Length(max = 11)
    private Integer phone;

    @Column
    private String photoUrl;

    @OneToOne
    @NonNull
    private Region region;

    public MarkerDto toMarkerDto() {
        return MarkerDto.builder()
                .address(address).category(category)
                .etc(etc).idx(idx).lat(lat).lng(lng)
                .name(name).phone(phone).photoUrl(photoUrl)
                .region(region.toRegionDto())
                .build();
    }
}