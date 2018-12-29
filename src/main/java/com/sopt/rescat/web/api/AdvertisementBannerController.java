package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.AdvertisementBanner;
import com.sopt.rescat.dto.ExceptionDto;
import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.service.AdvertisementBannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(value = "AdvertisementBannerController", description = "광고 배너 api")
@Controller
@RequestMapping("/api/banners/advertisement-banners")
public class AdvertisementBannerController {
    private AdvertisementBannerService advertisementBannerService;

    public AdvertisementBannerController(final AdvertisementBannerService advertisementBannerService) {
        this.advertisementBannerService = advertisementBannerService;
    }

    @ApiOperation(value = "광고 배너 전체 리스트", notes = "광고 배너 전체 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "광고 배너 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<AdvertisementBanner>> list() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementBannerService.gets());
    }

    @ApiOperation(value = "랜덤 광고 배너", notes = "랜덤으로 광고 배너 하나를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "랜덤 광고 배너 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/random")
    public ResponseEntity<BannerDto> getByRandomIdx() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementBannerService.getByRandomIdx());
    }
}
