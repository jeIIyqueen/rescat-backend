package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.FundingPhoto;

import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.NotExistException;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import lombok.Getter;
import lombok.NonNull;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funding extends BaseEntity {
    @Transient
    private final static int MAIN_PHOTO_INDEX = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FundingComment> comments;

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
    private Long currentAmount = 0L;

    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Bank bankName;

    @Column
    @NonNull
    private String account;

    @Column
    @NonNull
    private String mainRegion;


    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    private List<FundingPhoto> certifications;

    @Column
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL)
    private List<FundingPhoto> photos;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date limitAt;

    @Column
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @Transient
    private String nickname;

    public Funding setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public FundingResponseDto toFundingDto() {
        if (photos.size() == MAIN_PHOTO_INDEX) throw new NotExistException("photo", "해당 글의 사진이 등록되어 있지 않습니다.");

        return FundingResponseDto.builder()
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

    public void updateCurrentAmount(Long amount) {
        this.currentAmount += amount;
    }

    public Funding setWriter(User writer) {
        initWriter(writer);
        return this;
    }

    public Funding initCertifications(List<FundingPhoto> certificationPhotos) {
        this.certifications = certificationPhotos;
        return this;
    }

    public Funding initPhotos(List<FundingPhoto> photos) {
        this.photos = photos;
        return this;
    }

    public void updateConfirmStatus(Integer isConfirmed) {
        this.isConfirmed = isConfirmed;
    }
}
