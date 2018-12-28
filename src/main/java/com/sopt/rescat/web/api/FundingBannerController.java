package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.service.FundingBannerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

    @ApiOperation(value = "펀딩 후기 광고 4개 리스트", notes = "펀딩 후기 광고 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "펀딩 후기 광고 4개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<BannerDto>> list() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingBannerService.get4banners());
    }
}
