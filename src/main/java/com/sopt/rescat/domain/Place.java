package com.sopt.rescat.domain;

import com.sopt.rescat.dto.MarkerDto;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Length(max = 13)
    @Pattern(regexp = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4}$")
    private String phone;

    @Column
    private String photoUrl;

    @OneToOne
    @NonNull
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_place_region_idx"))
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
