package com.sopt.rescat.dto.response;

import com.sopt.rescat.domain.photo.FundingPhoto;
import com.sopt.rescat.dto.CommentDto;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class FundingDto {
    private Long idx;
    private String title;
    private String introduction;
    private Long goalAmount;
    private Long currentAmount;
    private Integer category;
    private FundingPhoto mainPhoto;
    private Date limitAt;
}
