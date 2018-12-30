package com.sopt.rescat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MapRequestDto {
    @ApiModelProperty(hidden = true)
    private final Integer CONFIRM = 1;
    @ApiModelProperty(hidden = true)
    private final Integer DEFER   = 0;
    @ApiModelProperty(hidden = true)
    private final Integer REFUSE  = 2;

    @ApiModelProperty(hidden = true)
    private Long idx;

    @ApiModelProperty(notes = "요청 유형(0: 등록, 1: 수정)", position = 1)
    @Range(min = 0, max = 1)
    // 0: 등록, 1: 수정
    private Integer requestType;

    @ApiModelProperty(notes = "마커 유형(0: 배식소, 1: 병원, 2: 길고양이)", position = 2)
    @Range(min = 0, max = 2)
    // 0: 배식소, 1: 병원, 2: 고양이
    private Integer registerType;

    @ApiModelProperty(notes = "고양이 이름(10자이내) 또는 배식소, 병원 이름(50자이내)", position = 3)
    private String name;

    @ApiModelProperty(notes = "특징 또는 부가정보", position = 4)
    private String etc;

    @ApiModelProperty(notes = "위도 좌표", position = 5)
    private Float lat;

    @ApiModelProperty(notes = "경도 좌표", position = 6)
    private Float lng;

    @ApiModelProperty(notes = "(only병원)주소", position = 7)
    private String address;

    @ApiModelProperty(notes = "사진",position = 12)
    private MultipartFile photo;

    @ApiModelProperty(hidden = true)
    private String photoUrl;

    @ApiModelProperty(notes = "(only길고양이)활동반경", position = 8)
    // 아래는 고양이 고유
    private Integer radius;

    @ApiModelProperty(notes = "(only길고양이)성별(0: 남, 1: 여)", position = 9)
    @Range(min = 0, max = 1)
    // 0: 남, 1: 여
    private Integer sex;

    @ApiModelProperty(notes = "(only길고양이)추정나이", position = 10)
    private String age;

    @ApiModelProperty(notes = "(only길고양이)중성화여부(0: 미완료, 1: 완료)", position = 11)
    @Range(min = 0, max = 1)
    // 0: 미완료, 1: 완료
    private Integer tnr;

    public MapRequest toMapRequest(User user, String photoUrl) {
        return MapRequest.builder()
                .writer(user)
                .isConfirmed(DEFER)
                .requestType(requestType)
                .registerType(registerType)
                .name(name)
                .etc(etc)
                .lat(lat)
                .lng(lng)
                .photoUrl(photoUrl)
                .build();
    }
}
