package jellyqueen.rescat.web.api;

import jellyqueen.rescat.dto.response.BannerDto;
import jellyqueen.rescat.service.AdvertisementBannerService;
import jellyqueen.rescat.service.FundingBannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(value = "ApiBannerController", description = "배너 관련 api")
@Controller
@RequestMapping("/api/banners")
public class ApiBannerController {

    private AdvertisementBannerService advertisementBannerService;
    private FundingBannerService fundingBannerService;

    public ApiBannerController(final AdvertisementBannerService advertisementBannerService, final FundingBannerService fundingBannerService) {
        this.advertisementBannerService = advertisementBannerService;
        this.fundingBannerService = fundingBannerService;
    }

    @ApiOperation(value = "광고 배너 전체 리스트 조회", notes = "광고 배너 전체 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "광고 배너 리스트 반환 성공", response = BannerDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/advertisement")
    public ResponseEntity<Iterable<BannerDto>> getAdvertisementBannerList() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementBannerService.gets());
    }

    @ApiOperation(value = "랜덤 광고 배너 조회", notes = "랜덤으로 광고 배너 하나를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "랜덤 광고 배너 반환 성공", response = BannerDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/advertisement/random")
    public ResponseEntity<BannerDto> getByRandomIdx() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementBannerService.getByRandomIdx());
    }

    @ApiOperation(value = "펀딩 후기 배너 4개 리스트 조회", notes = "펀딩 후기 배너 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "펀딩 후기 배너 4개 리스트 반환 성공", response = BannerDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/funding-reviews")
    public ResponseEntity<Iterable<BannerDto>> getFundingBannerList() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingBannerService.get4banners());
    }
}
