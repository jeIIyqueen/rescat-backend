package org.sopt.appjam.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;

@Entity
public class Permission extends BaseTime {
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

    @Column
    private Integer tnr;
}
