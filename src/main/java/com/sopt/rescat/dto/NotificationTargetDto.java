package com.sopt.rescat.dto;

import com.sopt.rescat.domain.enums.RequestType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Getter
@Builder
public class NotificationTargetDto {


    @ApiModelProperty(notes = "관련 글 idx")
    private Long targetIdx;


    @ApiModelProperty(notes = "관련 글 type")
    private RequestType targetType;
}
