package com.sopt.rescat.dto.response;

import com.sopt.rescat.domain.photo.FundingPhoto;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class FundingResponseDto {
    private Long idx;
    private String title;
    private String introduction;
    private Long goalAmount;
    private Long currentAmount;
    private Integer category;
    private FundingPhoto mainPhoto;
    private Date limitAt;
}
