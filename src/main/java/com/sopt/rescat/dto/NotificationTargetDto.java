package com.sopt.rescat.dto;

import com.sopt.rescat.domain.CareApplication;
import com.sopt.rescat.domain.enums.RequestType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
public class NotificationTargetDto {


    @ApiModelProperty(notes = "관련 글 idx")
    private Long targetIdx;

    @ApiModelProperty(notes = "관련 글 type")
    private RequestType targetType;

    @ApiModelProperty(notes = "입양/임보 신청서")
    private CareApplication careApplication;

    @ApiModelProperty(notes = "입양=0 임보=1")
    private Integer applicationType;
}
