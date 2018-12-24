package com.sopt.rescat.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Cat extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lng;

    @Column(nullable = false)
    private Integer radius;

    @Column(nullable = false)
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
