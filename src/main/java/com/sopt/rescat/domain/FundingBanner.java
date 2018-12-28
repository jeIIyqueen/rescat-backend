package com.sopt.rescat.domain;

import com.sopt.rescat.dto.response.BannerDto;
import lombok.Getter;

import javax.persistence.*;

@Getter
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
