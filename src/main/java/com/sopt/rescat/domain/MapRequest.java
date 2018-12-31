package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Slf4j
@ToString
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MapRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private Long idx;

    @Column
    @NonNull
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "요청 유형(0: 등록, 1: 수정)", position = 1)
    // 0: 등록, 1: 수정
    private Integer requestType;

    @Column
    @NonNull
    @Range(min = 0, max = 2)
    @ApiModelProperty(notes = "마커 유형(0: 배식소, 1: 병원, 2: 길고양이)", position = 2)
    // 0: 고양이, 1: 배식소, 2: 병원
    private Integer registerType;

    @Column
    @NonNull
    @Length(max = 50)
    @ApiModelProperty(notes = "고양이 이름 또는 배식소, 병원 이름(50자이내)", position = 3)
    private String name;

    @Column
    @ApiModelProperty(notes = "특징 또는 부가정보", position = 4)
    private String etc;

    @NonNull
    @Column
    @ApiModelProperty(notes = "위도 좌표", position = 5)
    private Double lat;

    @NonNull
    @Column
    @ApiModelProperty(notes = "경도 좌표", position = 6)
    private Double lng;

    @ApiModelProperty(notes = "(only병원)주소", position = 7)
    private String address;

    @ApiModelProperty(hidden = true)
    @OneToOne
    @NonNull
    @JsonIgnore
    private Region region;

    @URL
    @Column
    private String photoUrl;

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
    @ApiModelProperty(hidden = true)
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @Transient
    private String regionFullName;

    @Column
    @ApiModelProperty(notes = "(수정요청시)마커의 idx", position = 12)
    private Long markerIdx;

    @Builder
    public MapRequest(User writer, @NonNull @Range(min = 0, max = 1) Integer requestType, @NonNull @Range(min = 0, max = 2) Integer registerType, @NonNull @Length(max = 50) String name, String etc, @NonNull Double lat, @NonNull Double lng, String address, @NonNull Region region, @URL String photoUrl, Integer radius, @Range(min = 0, max = 1) Integer sex, @Length(max = 5) String age, @Range(min = 0, max = 1) Integer tnr, @Range(min = 0, max = 2) Integer isConfirmed, Long markerIdx) {
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
    }

    public boolean hasMarkerIdx(){
        return this.markerIdx != null;
    }

    public boolean isEditCategory(){
        return this.requestType == 1;
    }
}