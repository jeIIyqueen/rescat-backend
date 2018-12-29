package com.sopt.rescat.dto.response;

import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.photo.CertificationPhoto;
import com.sopt.rescat.domain.photo.FundingPhoto;
import com.sopt.rescat.dto.CommentDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class FundingDetailDto {
    private Long idx;
    private String writer;
    private List<CommentDto> comments;
    private String title;
    private String contents;
    private String introduction;
    private Long goalAmount;
    private Long currentAmount;
    private Bank bankName;
    private String account;
    private List<CertificationPhoto> certifications;
    private Integer category;
    private List<FundingPhoto> photos;
    private Date limitAt;
    private LocalDateTime createdAt;
}
