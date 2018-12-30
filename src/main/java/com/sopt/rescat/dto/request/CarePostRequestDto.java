package com.sopt.rescat.dto.request;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CarePostRequestDto {
    // 0: 입양, 1: 임시보호
    @Range(min = 0, max = 1)
    private Integer type;

    // 0: 남, 1: 여
    @Range(min = 0, max = 1)
    private Integer sex;

    @Range(min = 0, max = 1)
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Size(min = 1, max = 3)
    private List<String> photoUrls;

    @ApiModelProperty(example = "2살 추정")
    private String age;

    private String contents;
    private String name;
    private Breed breed;
    private Vaccination vaccination;
    private String etc;
    private LocalDateTime startProtectionPeriod;
    private LocalDateTime endProtectionPeriod;

    public CarePost toCarePost() {
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
                .isConfirmed(false)
                .build();
    }

    public List<CarePostPhoto> convertPhotoUrlsToCarePostPhoto(CarePost carePost) {
        return this.photoUrls.stream()
                .map(CarePostPhoto::new)
                .map(carePostPhoto -> carePostPhoto.initCarePost(carePost))
                .collect(Collectors.toList());
    }
}
