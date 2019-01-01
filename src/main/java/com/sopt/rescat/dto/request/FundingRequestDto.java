package com.sopt.rescat.dto.request;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.FundingPhoto;
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
    @Range
    @NonNull
    private Long goalAmount;

    // 0: 치료비 모금, 1: 프로젝트 후원
    @Range(min = 0, max = 1)
    @NonNull
    private Integer category;

    @Size(min = 1, max = 3)
    @NonNull
    private List<String> certificationUrls;

    @Size(min = 1, max = 3)
    @NonNull
    private List<String> photoUrls;

    @NonNull
    private String title;
    @NonNull
    private String contents;
    @NonNull
    private String introduction;
    @NonNull
    private Bank bankName;
    @NonNull
    private String account;
    @NonNull
    private String mainRegion;
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
