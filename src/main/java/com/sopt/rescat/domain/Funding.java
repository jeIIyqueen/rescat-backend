package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.CertificationPhoto;
import com.sopt.rescat.domain.photo.FundingPhoto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.NotExistException;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Funding extends BaseEntity {
    @Transient
    private final static int MAIN_PHOTO_INDEX = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true, notes = "펀딩 글 번호")
    private Long idx;

    @ApiModelProperty(notes = "댓글 리스트", readOnly = true)
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FundingComment> comments;

    @ApiModelProperty(notes = "글 제목", required = true)
    @Column(length = 100)
    @NonNull
    private String title;

    @ApiModelProperty(notes = "글 내용", required = true)
    @Column(length = 500)
    @NonNull
    private String contents;

    @ApiModelProperty(notes = "자기소개")
    @Column
    private String introduction;

    @ApiModelProperty(notes = "이름", required = true)
    @Column
    @NonNull
    private String name;

    @ApiModelProperty(notes = "전화번호", required = true)
    @Column
    @NonNull
    @Pattern(regexp = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})([0-9]{3,4})([0-9]{4})$", message = "잘못된 전화번호 형식입니다..")
    private String phone;

    @ApiModelProperty(notes = "목표금액", required = true)
    @Column
    @NonNull
    private Long goalAmount;

    @ApiModelProperty(notes = "현재금액", required = true)
    @Column
    @NonNull
    @Builder.Default
    private Long currentAmount = 0L;

    @ApiModelProperty(notes = "은행명", required = true)
    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Bank bankName;

    @ApiModelProperty(notes = "계좌번호", required = true)
    @Column
    @NonNull
    private String account;

    @ApiModelProperty(notes = "프로젝트 진행 지역(프로젝트 후원)/구조 지역(치료비 모금)", required = true)
    @Column
    @NonNull
    private String mainRegion;

    @ApiModelProperty(notes = "증빙서류 사진", required = true)
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CertificationPhoto> certifications;

    @ApiModelProperty(notes = "펀딩글 유형(0: 치료비 모금, 1: 프로젝트 후원)", required = true)
    @Column
    @NonNull
    @Range(min = 0, max = 1)
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;

    @ApiModelProperty(notes = "사진 리스트")
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FundingPhoto> photos;

    @ApiModelProperty(notes = "마감 기한")
    @Column
    private LocalDateTime limitAt;

    @ApiModelProperty(notes = "관리자 승인 여부(0: 보류, 1: 승인, 2: 거절)")
    @Column
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @ApiModelProperty(notes = "신고")
    @Column
    @Builder.Default
    private int warning = 0;

    @ApiModelProperty(notes = "작성자 닉네임", readOnly = true)
    @Transient
    private String nickname;

    @ApiModelProperty(notes = "작성자 일치 여부", readOnly = true)
    @Transient
    private Boolean isWriter;

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
                .name(name)
                .phone(phone)
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

    public Funding initCertifications(List<CertificationPhoto> certificationPhotos) {
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

    private boolean equalsWriter(User loginUser) {
        return this.getWriter().equals(loginUser);
    }

    public Funding setStatus(User loginUser) {
        this.isWriter = this.equalsWriter(loginUser);
        return this;
    }

    public void warningCount() {
        ++this.warning;
    }

    public boolean isAvailableRefund() {
        return this.currentAmount < this.goalAmount;
    }
}
