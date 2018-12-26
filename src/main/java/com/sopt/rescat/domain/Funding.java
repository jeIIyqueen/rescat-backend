package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Funding extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToMany
    private List<Comment> comments;

    @Column(length = 100)
    @NonNull
    private String title;

    @Column(length = 500)
    @NonNull
    private String contents;

    @Column
    @NonNull
    private Long goalAmount;

    @Column
    @NonNull
    private Long currentAmount;

    @Column
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;

    @OneToMany
    private List<Photo> photos;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date limitAt;
}
