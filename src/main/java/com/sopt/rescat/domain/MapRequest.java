package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.RequestStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Pattern;

@Slf4j
@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MapRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true)
    private Long idx;

    @Column
    @NonNull
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "요청 유형(0: 등록, 1: 수정)", position = 1, required = true)
    // 0: 등록, 1: 수정
    private Integer requestType;

    @Column
    @NonNull
    @Range(min = 0, max = 2)
    @ApiModelProperty(notes = "마커 유형(0: 배식소, 1: 병원, 2: 길고양이)", position = 2, required = true)
    // 0: 고양이, 1: 배식소, 2: 병원
    private Integer registerType;

    @Column
    @NonNull
    @Length(max = 50)
    @ApiModelProperty(notes = "고양이 이름 또는 배식소, 병원 이름(50자이내)", position = 3, required = true)
    private String name;

    @Column
    @ApiModelProperty(notes = "특징 또는 부가정보", position = 4)
    private String etc;

    @NonNull
    @Column
    @ApiModelProperty(notes = "위도 좌표", position = 5, required = true)
    private Double lat;

    @NonNull
    @Column
    @ApiModelProperty(notes = "경도 좌표", position = 6, required = true)
    private Double lng;

    @ApiModelProperty(hidden = true)
    @OneToOne
    @NonNull
    @JsonIgnore
    private Region region;

    @URL
    @Column
    @ApiModelProperty(notes = "사진", position = 13)
    private String photoUrl;

    @Column
    @ApiModelProperty(notes = "(only병원)주소", position = 7)
    private String address;

    @Column
    @Pattern(regexp = "^(null|(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4})$")
    @ApiModelProperty(notes = "(only병원)전화번호", position = 7)
    private String phone;

    @Column
    @ApiModelProperty(notes = "(only길고양이)활동반경", position = 8)
    private Integer radius;

    @Column
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "(only길고양이)성별(0: 남, 1: 여)", position = 9)
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    @Length(max = 5)
    @ApiModelProperty(notes = "(only길고양이)추정나이", position = 10)
    private String age;


    @Column
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "(only길고양이)중성화여부(0: 미완료, 1: 완료)", position = 11)
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Column
    @ApiModelProperty(notes = "(수정요청시 필수)마커의 idx", position = 12)
    private Long markerIdx;

    @Column
    @ApiModelProperty(readOnly = true)
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @Transient
    @ApiModelProperty(notes = "지역 전체 이름", position = 14, required = true)
    private String regionFullName;

    @ApiModelProperty(hidden = true)
    @Transient
    private String writerName;

    @Builder
    public MapRequest(User writer, @NonNull @Range(min = 0, max = 1) Integer requestType, @NonNull @Range(min = 0, max = 2) Integer registerType, @NonNull @Length(max = 50) String name, String etc, @NonNull Double lat, @NonNull Double lng, String address, @NonNull Region region, @URL String photoUrl, Integer radius, @Range(min = 0, max = 1) Integer sex, @Length(max = 5) String age, @Range(min = 0, max = 1) Integer tnr, @Range(min = 0, max = 2) Integer isConfirmed, Long markerIdx, @Length(max = 13) String phone) {
        super(writer);
        this.requestType = requestType;
        this.registerType = registerType;
        this.name = name;
        this.etc = etc;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.region = region;
        this.photoUrl = photoUrl;
        this.radius = radius;
        this.sex = sex;
        this.age = age;
        this.tnr = tnr;
        this.isConfirmed = isConfirmed;
        this.markerIdx = markerIdx;
        this.phone = phone;
    }

    public MapRequest setWriterName() {
        this.writerName = getWriter().getName();
        return this;
    }

    public MapRequest approve() {
        this.isConfirmed = RequestStatus.CONFIRM.getValue();
        return this;
    }

    public MapRequest refuse() {
        this.isConfirmed = RequestStatus.REFUSE.getValue();
        return this;
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public boolean hasMarkerIdx() {
        return this.markerIdx != null;
    }

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    public boolean isEditCategory() {
        return this.requestType == 1;
    }

    @ApiModelProperty(hidden = true)
    public Place toPlace() {
        return Place.builder().address(this.address).category(this.registerType).etc(this.etc).lat(this.lat)
                .lng(this.lng).name(this.name).phone(this.phone).photoUrl(this.photoUrl).region(region).writer(this.getWriter()).build();
    }

    @ApiModelProperty(hidden = true)
    public Cat toCat() {
        return Cat.builder().age(this.age).etc(this.etc).lat(this.lat).lng(this.lng).name(this.name)
                .photoUrl(this.photoUrl).radius(this.radius).region(this.region).sex(this.sex).tnr(this.tnr).writer(this.getWriter()).build();
    }
}