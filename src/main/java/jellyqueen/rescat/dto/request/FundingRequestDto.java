package jellyqueen.rescat.dto.request;

import jellyqueen.rescat.domain.Funding;
import jellyqueen.rescat.domain.enums.Bank;
import jellyqueen.rescat.domain.photo.CertificationPhoto;
import jellyqueen.rescat.domain.photo.FundingPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class FundingRequestDto {
    private static final Integer CATEGORY_PROJECT = 1;

    @ApiModelProperty(notes = "목표금액", required = true)
    @Range(min = 10000, message = "최소 목표금액은 10000원입니다.")
    @NonNull
    private Long goalAmount;

    @ApiModelProperty(notes = "펀딩글 유형(0: 치료비 모금, 1: 프로젝트 후원)", required = true)
    // 0: 치료비 모금, 1: 프로젝트 후원
    @Range(min = 0, max = 1)
    @NonNull
    private Integer category;

    @ApiModelProperty(notes = "증빙서류 사진 url 리스트", required = true)
    private List<@URL String> certificationUrls;

    @ApiModelProperty(notes = "사진 url 리스트", required = true)
    @Size(min = 1, max = 3)
    @NotNull
    @NonNull
    private List<@URL String> photoUrls;

    @ApiModelProperty(notes = "글 제목", required = true)
    @NonNull
    private String title;

    @ApiModelProperty(notes = "글 내용", required = true)
    @NonNull
    private String contents;

    @ApiModelProperty(notes = "자기소개")
    private String introduction;

    @ApiModelProperty(notes = "이름", required = true)
    @NonNull
    @Length(max = 10)
    private String name;

    @ApiModelProperty(notes = "전화번호", required = true)
    @NonNull
    @Pattern(regexp = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})([0-9]{3,4})([0-9]{4})$", message = "잘못된 전화번호 형식입니다.")
    private String phone;

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
//    @FutureOrPresent(message = "마감기한은 과거날짜일 수 없습니다. ")
    private LocalDateTime limitAt;

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
                .name(name)
                .phone(phone)
                .mainRegion(mainRegion)
                .limitAt(limitAt)
                .isConfirmed(0)
                .warning(0)
                .build();
    }

    public List<FundingPhoto> convertPhotoUrlsToPhotos(Funding funding) {
        return this.photoUrls.stream()
                .map(FundingPhoto::new)
                .peek(fundingPhoto -> fundingPhoto.initFunding(funding))
                .collect(Collectors.toList());
    }

    public List<CertificationPhoto> convertCertificationUrlsToCertifications(Funding funding) {
        if(funding.getCategory().equals(CATEGORY_PROJECT)) return null;

        return this.certificationUrls.stream()
                .map(CertificationPhoto::new)
                .peek(certificationPhoto -> certificationPhoto.initFunding(funding))
                .collect(Collectors.toList());
    }
}
