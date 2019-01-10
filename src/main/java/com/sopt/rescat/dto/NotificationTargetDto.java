package com.sopt.rescat.dto;

import com.sopt.rescat.domain.enums.RequestType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
public class NotificationTargetDto {
    private Long targetIdx;
    private RequestType targetType;
}
