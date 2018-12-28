package com.sopt.rescat.domain;

import lombok.Builder;
import lombok.NonNull;

import javax.persistence.*;

@Entity
public class MapRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    // 0: 등록, 1: 수정
    private Integer requestType;

    @Column
    @NonNull
    // 0: 고양이, 1: 배식소, 2: 병원
    private Integer registerType;

    @Column
    @NonNull
    private String name;

    @Column
    private String etc;

    @Column
    private Float lat;

    @Column
    private Float lng;

    @OneToOne
    @NonNull
    private Region region;

    @OneToOne
    private Photo photo;

    // 아래는 고양이 고유
    @Column
    private Integer radius;

    @Column
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    private String age;

    @Column
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Builder
    public MapRequest(User writer, Integer isConfirmed, @NonNull Integer requestType, @NonNull Integer registerType, @NonNull String name, String etc, Float lat, Float lng, Photo photo, Integer radius, Integer sex, String age, Integer tnr) {
        super(writer, isConfirmed);
        this.requestType = requestType;
        this.registerType = registerType;
        this.name = name;
        this.etc = etc;
        this.lat = lat;
        this.lng = lng;
        this.photo = photo;
        this.radius = radius;
        this.sex = sex;
        this.age = age;
        this.tnr = tnr;
    }
}
