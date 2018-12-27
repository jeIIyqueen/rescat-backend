package com.sopt.rescat.domain;

import javax.persistence.*;

@Entity
public class FundingBanner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne
    private Photo photo;

    @Column
    private String title;

    @OneToOne
    private Funding funding;
}
