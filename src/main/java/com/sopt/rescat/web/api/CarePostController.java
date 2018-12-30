package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.dto.response.CarePostDto;
import com.sopt.rescat.service.CarePostService;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "CarePostController", description = "입양/임시보호 글 관련 api")
@RestController
@RequestMapping("/api/care-posts")
public class CarePostController {

    private CarePostService carePostService;

    public CarePostController(CarePostService carePostService) {
        this.carePostService = carePostService;
    }

    @ApiOperation(value = "입양/임시보호 글 중 최신 5개 리스트 조회", notes = "입양/임시보호 글 중 최신 5개 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 중 최신 5개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<CarePostDto>> get5Post() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.find5Post());
    }

    @ApiOperation(value = "입양 글 리스트 또는 임시보호 글 리스트 조회", notes = "입양 글 리스트 또는 임시보호 글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양 글 리스트 또는 임시보호 글 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{type}")
    public ResponseEntity<Iterable<CarePostDto>> getAllBy(
            @ApiParam(value = "0: 입양, 1: 임시보호", required = true)
            @PathVariable Integer type) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findAllBy(type));
    }
}
