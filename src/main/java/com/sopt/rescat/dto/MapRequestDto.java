package com.sopt.rescat.dto;

import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.Photo;
import com.sopt.rescat.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.Transient;
import java.util.List;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MapRequestDto {
    @ApiModelProperty(hidden = true)
    private Long idx;

    @ApiModelProperty(example = "0", notes = "0: 등록, 1: 수정", position = 1, dataType = "java.lang.integer")
    @Range(min = 0, max = 1)
    // 0: 등록, 1: 수정
    private Integer requestType;

    @ApiModelProperty(example = "0", notes = "0: 고양이, 1: 배식소, 2: 병원", position = 2, dataType = "java.lang.integer")
    @Range(min = 0, max = 2)
    // 0: 고양이, 1: 배식소, 2: 병원
    private Integer registerType;

    @ApiModelProperty(example = "뿌꾸", notes = "10자 이내", position = 3)
    @Length(max = 10)
    private String name;

    @ApiModelProperty(example = "손을 자주 문다.", position = 4)
    private String etc;

    @ApiModelProperty(example = "37.4875908", notes = "위도", position = 5)
    private Float lat;

    @ApiModelProperty(example = "126.9194145", notes = "경도", position = 6)
    private Float lng;

    @ApiModelProperty(position = 7)
    private MultipartFile photo;

    @ApiModelProperty(example = "500", position = 8, dataType = "java.lang.integer")
    // 아래는 고양이 고유
    private Integer radius;

    @ApiModelProperty(example = "0", notes = "0: 남, 1: 여", position = 9, dataType = "java.lang.integer")
    @Range(min = 0, max = 1)
    // 0: 남, 1: 여
    private Integer sex;

    @ApiModelProperty(example = "7개월", notes = "추정 나이", position = 10)
    private String age;

    @ApiModelProperty(example = "0", notes = "중성화 여부/0: 미완료, 1: 완료", position = 11, dataType = "java.lang.integer")
    @Range(min = 0, max = 1)
    // 0: 미완료, 1: 완료
    private Integer tnr;

    public MapRequest toMapRequest(User user, Photo photo) {
        return MapRequest.builder()
                .writer(user)
                .isConfirmed(false)
                .requestType(requestType)
                .registerType(registerType)
                .name(name)
                .etc(etc)
                .lat(lat)
                .lng(lng)
                .photo(photo)
                .radius(radius)
                .sex(sex)
                .age(age)
                .tnr(tnr)
                .build();
    }
}
