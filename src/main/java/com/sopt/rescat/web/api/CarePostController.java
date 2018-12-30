package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.dto.response.CarePostDto;
import com.sopt.rescat.service.CarePostService;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(value = "CarePostController", description = "입양/임시보호 글 관련 api")
@RestController
@RequestMapping("/api/care-posts")
public class CarePostController {

    private CarePostService carePostService;

    public CarePostController(CarePostService carePostService) {
        this.carePostService = carePostService;
    }

    @ApiOperation(value = "입양 글 리스트 또는 임시보호 글 리스트 조회", notes = "입양 글 리스트 또는 임시보호 글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양 글 리스트 또는 임시보호 글 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<CarePostDto>> getAllBy(
            @ApiParam(value = "0: 입양, 1: 임시보호", required = true)
            @RequestParam Integer type) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findAllBy(type));
    }

    @ApiOperation(value = "입양/임시보호 글 조회", notes = "idx 에 따른 입양/임시보호 글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 반환 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}")
    public ResponseEntity<CarePost> getPostByIdx(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findBy(idx));
    }

    @ApiOperation(value = "입양/임시보호 글의 댓글 조회", notes = "idx에 해당하는 입양/임시보호 글의 댓글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "idx에 해당하는 입양/임시보호 글의 댓글 리스트 반환 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}/comments")
    public ResponseEntity<Iterable<CarePostComment>> getComments(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCommentsBy(idx));
    }

    @ApiOperation(value = "입양/임시보호 글 중 최신 5개 리스트 조회", notes = "입양/임시보호 글 중 최신 5개 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 중 최신 5개 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/main")
    public ResponseEntity<Iterable<CarePostDto>> get5Post() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.find5Post());
    }
}
