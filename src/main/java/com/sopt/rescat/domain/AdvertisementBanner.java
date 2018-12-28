package com.sopt.rescat.domain;

import com.sopt.rescat.dto.response.BannerDto;

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

    @Column
    private String link;

    public BannerDto toBannerDto() {
        return BannerDto.builder()
                .idx(idx)
                .link(link)
                .photoUrl(photo.getUrl())
                .title(title)
                .build();
    }
}
