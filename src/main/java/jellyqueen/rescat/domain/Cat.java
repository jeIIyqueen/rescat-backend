package jellyqueen.rescat.domain;

import jellyqueen.rescat.domain.request.MapRequest;
import jellyqueen.rescat.dto.MarkerDto;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class Cat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 10, nullable = false)
    @NotNull
    private String name;

    @Column
    @NonNull
    @NotNull
    @Range(min = 33, max = 43)
    private Double lat;

    @Column
    @NonNull
    @NotNull
    @Range(min = 124, max = 132)
    private Double lng;

    @Column
    @NonNull
    @NotNull
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    @Length(max = 10)
    private String age;

    @Column
    @NonNull
    @NotNull
    // 0: 미완료, 1: 완료, 2: 모름
    private Integer tnr;

    @Column
    @Length(max = 45)
    private String etc;

    @URL
    @Column
    private String photoUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @NonNull
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_cat_region_idx"))
    private Region region;

    @Transient
    @NotNull
    private String regionFullName;

    @Builder
    public Cat(User writer, String name, @NonNull Double lat, @NonNull Double lng, @NonNull Integer sex, String age, Integer tnr, String etc, String photoUrl, @NonNull Region region, String regionFullName) {
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
        this.regionFullName = regionFullName;
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
