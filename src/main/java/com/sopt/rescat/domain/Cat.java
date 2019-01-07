package com.sopt.rescat.domain;

import com.sopt.rescat.dto.MarkerDto;
import lombok.*;

import javax.persistence.*;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class Cat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 10, nullable = false)
    private String name;

    @Column
    @NonNull
    private Double lat;

    @Column
    @NonNull
    private Double lng;

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

    @Builder
    public Cat(User writer, String name, @NonNull Double lat, @NonNull Double lng, @NonNull Integer sex, String age, Integer tnr, String etc, String photoUrl, @NonNull Region region) {
        super(writer);
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.sex = sex;
        this.age = age;
        this.tnr = tnr;
        this.etc = etc;
        this.photoUrl = photoUrl;
        this.region = region;
    }

    public MarkerDto toMarkerDto() {
        return MarkerDto.builder()
                .category(2)
                .age(age).etc(etc).idx(idx).lat(lat).lng(lng)
                .name(name).photoUrl(photoUrl)
                .region(region.toRegionDto()).sex(sex).tnr(tnr)
                .build();
    }

    public void update(MapRequest mapRequest) {
        this.age = mapRequest.getAge();
        this.etc = mapRequest.getEtc();
        this.lat = mapRequest.getLat();
        this.lng = mapRequest.getLng();
        this.name = mapRequest.getName();
        this.photoUrl = mapRequest.getPhotoUrl();
        this.region = mapRequest.getRegion();
        this.sex = mapRequest.getSex();
        this.tnr = mapRequest.getTnr();
        initWriter(mapRequest.getWriter());
    }
}
