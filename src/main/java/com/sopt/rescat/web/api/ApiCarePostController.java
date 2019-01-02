package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.service.CarePostService;
import com.sopt.rescat.utils.auth.AdminAuth;
import com.sopt.rescat.utils.auth.AuthAspect;
import com.sopt.rescat.utils.auth.CareTakerAuth;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(value = "ApiCarePostController", description = "입양/임시보호 글 관련 api")
@RestController
@RequestMapping("/api/care-posts")
public class ApiCarePostController {

    private CarePostService carePostService;

    public ApiCarePostController(CarePostService carePostService) {
        this.carePostService = carePostService;
    }

    @ApiOperation(value = "입양 글 리스트 또는 임시보호 글 리스트 조회", notes = "입양 글 리스트 또는 임시보호 글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양 글 리스트 또는 임시보호 글 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<CarePostResponseDto>> getAllBy(
            @ApiParam(value = "0: 입양, 1: 임시보호", required = true)
            @RequestParam Integer type) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findAllBy(type));
    }

    @ApiOperation(value = "입양/임시보호 글 등록", notes = "입양/임시보호 글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 등록 성공"),
            @ApiResponse(code = 400, message = "파라미터 형식 오류"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @CareTakerAuth
    @PostMapping("")
    public ResponseEntity<Void> create(
            @RequestHeader(value = "Authorization") final String token,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CarePostRequestDto carePostRequestDto) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.create(carePostRequestDto, loginUser);
        return ResponseEntity.ok().build();
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
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCarePostBy(idx));
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
    public ResponseEntity<Iterable<CarePostResponseDto>> get5Post() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.find5Post());
    }

    @ApiOperation(value = "품종 리스트 조회", notes = "품종 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "품종 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/breeds")
    public ResponseEntity<Iterable<Breed>> getBreeds() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.getBreeds());
    }
}
