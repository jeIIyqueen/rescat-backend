package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Cat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 10, nullable = false)
    private String name;

    @Column
    @NonNull
    private Float lat;

    @Column
    @NonNull
    private Float lng;

    @Column
    @NonNull
    private Integer radius;

    @Column
    @NonNull
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    private LocalDateTime birth;

    @Column
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Column
    private String etc;

    @OneToOne
    private Photo photo;
}
