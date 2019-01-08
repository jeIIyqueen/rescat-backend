package com.sopt.rescat.dto;

import com.sopt.rescat.domain.enums.RequestType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Getter
@Builder
public class NotificationTargetDto {
    private Long targetIdx;
    private RequestType targetType;
}
