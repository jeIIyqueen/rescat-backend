package com.sopt.rescat.domain;

import com.sopt.rescat.dto.MarkerDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Pattern;

@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true)
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
    @Range(min = 33, max = 43)
    private Double lat;

    @Column
    @NonNull
    @Range(min = 124, max = 132)
    private Double lng;

    @Column
    private String etc;

    @Column
    @NonNull
    private String address;

    @Column
    @Length(max = 13)
    @Pattern(regexp = "^(null|(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4})$")
    private String phone;

    @URL
    @Column
    private String photoUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @NonNull
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_place_region_idx"))
    private Region region;

    @Transient
    private String regionFullName;

    @Builder
    public Place(User writer, @NonNull Integer category, @NonNull @Length(max = 50) String name, @NonNull Double lat, @NonNull Double lng, String etc, @NonNull String address, @Length(max = 13) @Pattern(regexp = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4}$") String phone, String photoUrl, @NonNull Region region) {
        super(writer);
        this.category = category;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.etc = etc;
        this.address = address;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.region = region;
    }

    public MarkerDto toMarkerDto() {
        return MarkerDto.builder()
                .address(address).category(category)
                .etc(etc).idx(idx).lat(lat).lng(lng)
                .name(name).phone(phone).photoUrl(photoUrl)
                .region(region.toRegionDto())
                .build();
    }

    public void update(MapRequest mapRequest) {
        this.address = mapRequest.getAddress();
        this.etc = mapRequest.getEtc();
        this.lat = mapRequest.getLat();
        this.lng = mapRequest.getLng();
        this.name = mapRequest.getName();
        this.phone = mapRequest.getPhone();
        this.photoUrl = mapRequest.getPhotoUrl();
        this.region = mapRequest.getRegion();
        this.initWriter(mapRequest.getWriter());
    }
}