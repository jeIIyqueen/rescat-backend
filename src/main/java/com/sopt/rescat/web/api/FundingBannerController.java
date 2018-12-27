package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.service.FundingBannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funding-banners")
public class FundingBannerController {
    private FundingBannerService fundingBannerService;

    public FundingBannerController(final FundingBannerService fundingBannerService) {
        this.fundingBannerService = fundingBannerService;
    }

    @GetMapping("")
    public ResponseEntity<Iterable<BannerDto>> list() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingBannerService.get4banners());
    }
}
