package com.sopt.rescat.dto.request;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CarePostRequestDto {
    // 0: 입양, 1: 임시보호
    @ApiModelProperty(notes = "글 타입(0: 입양, 1: 임시보호)")
    @Range(min = 0, max = 1)
    @NotNull
    private Integer type;

    // 0: 남, 1: 여
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "성별(0: 남, 1: 여)")
    @NotNull
    private Integer sex;

    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "중성화 여부(0: 미완료, 1: 완료)")
    @NotNull
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Size(min = 1, max = 3)
    @ApiModelProperty(notes = "사진 url 리스트")
    @NotNull
    private List<@URL String> photoUrls;

    @ApiModelProperty(example = "2살 추정", notes = "나이")
    @NotNull
    private String age;

    @ApiModelProperty(notes = "글 내용", required = true)
    @NotNull
    private String contents;

    @ApiModelProperty(notes = "고양이 이름", required = true)
    @NotNull
    private String name;

    @ApiModelProperty(notes = "품종", required = true)
    @NotNull
    private Breed breed;

    @ApiModelProperty(notes = "예방접종여부", required = true)
    @NotNull
    private Vaccination vaccination;

    @ApiModelProperty(notes = "추가적인 말")
    private String etc;

    @ApiModelProperty(notes = "임시보호 시작기간(글 타입 1일 경우 필수)")
    private LocalDateTime startProtectionPeriod;

    @ApiModelProperty(notes = "임시보호 종료기간(글 타입 1일 경우 필수)")
    private LocalDateTime endProtectionPeriod;

    public CarePost toCarePost(Boolean isFinished) {
        return CarePost.builder()
                .age(age)
                .breed(breed)
                .contents(contents)
                .startProtectionPeriod(startProtectionPeriod)
                .endProtectionPeriod(endProtectionPeriod)
                .name(name)
                .sex(sex)
                .tnr(tnr)
                .type(type)
                .vaccination(vaccination)
                .isConfirmed(0)
                .isFinished(isFinished)
                .build();
    }

    public List<CarePostPhoto> convertPhotoUrlsToCarePostPhoto(CarePost carePost) {
        return this.photoUrls.stream()
                .map(CarePostPhoto::new)
                .map(carePostPhoto -> carePostPhoto.initCarePost(carePost))
                .collect(Collectors.toList());
    }
}
