package com.sopt.rescat.dto.request;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.FundingPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class FundingRequestDto {
    @ApiModelProperty(notes = "목표금액", required = true)
    @Range
    @NonNull
    private Long goalAmount;

    @ApiModelProperty(notes = "펀딩글 유형(0: 치료비 모금, 1: 프로젝트 후원)", required = true)
    // 0: 치료비 모금, 1: 프로젝트 후원
    @Range(min = 0, max = 1)
    @NonNull
    private Integer category;

    @ApiModelProperty(notes = "증빙서류 사진 url 리스트", required = true)
    @Size(min = 1, max = 3)
    @NonNull
//    @URL
    private List<String> certificationUrls;

    @ApiModelProperty(notes = "사진 url 리스트")
    @Size(min = 1, max = 3)
    @NonNull
//    @URL
    private List<String> photoUrls;

    @ApiModelProperty(notes = "글 제목", required = true)
    @NonNull
    private String title;

    @ApiModelProperty(notes = "글 내용", required = true)
    @NonNull
    private String contents;

    @ApiModelProperty(notes = "자기소개", required = true)
    @NonNull
    private String introduction;

    @ApiModelProperty(notes = "은행명", required = true)
    @NonNull
    private Bank bankName;

    @ApiModelProperty(notes = "계좌번호", required = true)
    @NonNull
    private String account;

    @ApiModelProperty(notes = "프로젝트 진행 지역(프로젝트 후원)/구조 지역(치료비 모금)", required = true)
    @NonNull
    private String mainRegion;

    @ApiModelProperty(notes = "마감 기한", required = true)
    @NonNull
    private Date limitAt;

    public Funding toFunding() {
        return Funding.builder()
                .goalAmount(goalAmount)
                .currentAmount(0L)
                .account(account)
                .bankName(bankName)
                .category(category)
                .title(title)
                .contents(contents)
                .introduction(introduction)
                .mainRegion(mainRegion)
                .limitAt(limitAt)
                .isConfirmed(0)
                .build();
    }

    public List<FundingPhoto> convertPhotoUrlsToPhotos(Funding funding) {
        return this.photoUrls.stream()
                .map(FundingPhoto::new)
                .map(fundingPhoto -> fundingPhoto.initFunding(funding))
                .collect(Collectors.toList());
    }

    public List<FundingPhoto> convertCertificationUrlsToCertifications(Funding funding) {
        return this.certificationUrls.stream()
                .map(FundingPhoto::new)
                .peek(FundingPhoto::setCertification)
                .map(fundingPhoto -> fundingPhoto.initFunding(funding))
                .collect(Collectors.toList());
    }
}
