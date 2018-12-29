package com.sopt.rescat.domain;

import com.sopt.rescat.dto.response.BannerDto;
import lombok.NonNull;

import javax.persistence.*;

@Entity
public class AdvertisementBanner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String photoUrl;

    @Column
    private String title;

    @Column
    private String link;

    public BannerDto toBannerDto() {
        return BannerDto.builder()
                .idx(idx)
                .link(link)
                .photoUrl(photoUrl)
                .title(title)
                .build();
    }
}
