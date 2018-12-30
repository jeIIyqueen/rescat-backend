package com.sopt.rescat.dto.request;

import com.sopt.rescat.domain.enums.Bank;

import java.util.Date;
import java.util.List;

public class FundingRequestDto {
    private String title;
    private String contents;
    private String introduction;
    private Long goalAmount;
    private Bank bankName;
    private String account;
    private String mainRegion;
    private List<String> certificationUrls;
    // 0: 치료비 모금, 1: 프로젝트 후원
    private Integer category;
    private List<String> photoUrls;
    private Date limitAt;
}
