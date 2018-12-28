package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.Bank;
import lombok.NonNull;

import javax.persistence.*;
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
    private String introduction;

    @Column
    @NonNull
    private Long goalAmount;

    @Column
    @NonNull
    private Long currentAmount;

    @Column
    @NonNull
    private Bank bankName;

    @Column
    @NonNull
    private String account;

    @Column
    @NonNull
    private String mainRigion;

    @OneToMany
    @NonNull
    private List<Photo> certifications;

    @Column
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;

    @OneToMany
    private List<Photo> photos;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date limitAt;
}
