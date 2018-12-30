package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.FundingComment;
import com.sopt.rescat.dto.response.FundingDto;
import com.sopt.rescat.service.FundingService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fundings")
public class FundingController {
    private final String PARAM_REX = "^[0,1]$";
    private FundingService fundingService;

    public FundingController(final FundingService fundingService) {
        this.fundingService = fundingService;
    }

    @ApiOperation(value = "치료비 모금/ 프로젝트 모금 리스트 조회", notes = "category에 따라 펀딩 글 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "치료비 모금/ 프로젝트 모금 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<FundingDto>> getAllBy(
            @ApiParam(value = "0: 치료비 모금, 1: 프로젝트", required = true)
            @RequestParam Integer category) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findAllBy(category));
    }

    @ApiOperation(value = "크라우드 펀딩 글 조회", notes = "idx 에 따른 크라우드 펀딩 글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 반환 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}")
    public ResponseEntity<Funding> getFundingByIdx(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findByIdx(idx));
    }

    @ApiOperation(value = "크라우드 펀딩 글의 댓글 조회", notes = "idx 에 따른 크라우드 펀딩 글의 댓글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글의 댓글 리스트 반환 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}/comments")
    public ResponseEntity<Iterable<FundingComment>> getComments(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findCommentsBy(idx));
    }

    @ApiOperation(value = "펀딩 글 4개 리스트", notes = "펀딩 글 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "펀딩 글 4개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/main")
    public ResponseEntity<Iterable<FundingDto>> get4Fundings() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.find4Fundings());
    }
}
