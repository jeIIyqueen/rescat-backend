package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.response.FundingDto;
import com.sopt.rescat.service.FundingService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fundings")
public class FundingController {
    private final String PARAM_REX = "^[0,1]$";
    private FundingService fundingService;

    public FundingController(final FundingService fundingService) {
        this.fundingService = fundingService;
    }

    @ApiOperation(value = "펀딩 글 4개 리스트", notes = "펀딩 글 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "펀딩 글 4개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<FundingDto>> get4Fundings() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.find4Fundings());
    }

    @ApiOperation(value = "펀딩 글 4개 리스트", notes = "펀딩 글 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "펀딩 글 4개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{category}")
    public ResponseEntity<Iterable<FundingDto>> getAllBy(
            @ApiParam(value = "0: 치료비 모금, 1: 프로젝트", required = true)
            @PathVariable Integer category) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findAllBy(category));
    }
}
