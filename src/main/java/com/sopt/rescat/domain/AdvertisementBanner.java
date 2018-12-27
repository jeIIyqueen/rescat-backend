package com.sopt.rescat.domain;

import javax.persistence.*;

@Entity
public class AdvertisementBanner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne
    private Photo photo;

    @Column
    private String title;
}
