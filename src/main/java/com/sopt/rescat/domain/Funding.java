package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.CertificationPhoto;
import com.sopt.rescat.domain.photo.FundingPhoto;
import com.sopt.rescat.dto.response.FundingDto;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Funding extends BaseEntity {
    private final static int MAIN_PHOTO_INDEX = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    private List<FundingComment> fundingComments;

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
    @Enumerated(EnumType.STRING)
    @NonNull
    private Bank bankName;

    @Column
    @NonNull
    private String account;

    @Column
    @NonNull
    private String mainRigion;

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    @NonNull
    private List<CertificationPhoto> certifications;

    @Column
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    private List<FundingPhoto> photos;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date limitAt;

    public FundingDto toFundingDto() {
        return FundingDto.builder()
                .idx(idx)
                .category(category)
                .currentAmount(currentAmount)
                .goalAmount(goalAmount)
                .introduction(introduction)
                .limitAt(limitAt)
                .title(title)
                .mainPhoto(photos.get(MAIN_PHOTO_INDEX))
                .build();
    }
}
