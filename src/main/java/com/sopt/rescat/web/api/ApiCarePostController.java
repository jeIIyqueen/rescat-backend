package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.CareApplication;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.service.CarePostService;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.auth.AdminAuth;
import com.sopt.rescat.utils.auth.Auth;
import com.sopt.rescat.utils.auth.AuthAspect;
import com.sopt.rescat.utils.auth.CareTakerAuth;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@Api(value = "ApiCarePostController", description = "입양/임시보호 글 관련 api")
@RestController
@RequestMapping("/api/care-posts")
public class ApiCarePostController {

    private CarePostService carePostService;
    private JWTService jwtService;
    private UserService userService;

    public ApiCarePostController(CarePostService carePostService,
                                 JWTService jwtService,
                                 UserService userService) {
        this.carePostService = carePostService;
        this.jwtService = jwtService;
        this.userService = userService;
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
            @ApiParam(value = "글 번호", required = true) @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCarePostBy(idx));
    }

    @ApiOperation(value = "입양/임시보호 글 승인", notes = "idx 에 따른 입양/임시보호 글을 승인합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 승인 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @AdminAuth
    @PutMapping("/{idx}")
    public ResponseEntity<Void> confirmPost(
            @RequestHeader(value = "Authorization") final String token,
            @PathVariable Long idx) {
        carePostService.confirmPost(idx);
        return ResponseEntity.status(HttpStatus.OK).build();
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

    @ApiOperation(value = "입양/임시보호 신청", notes = "입양/임시보호 글에 대한 신청을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "신청 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "입양/임시보호 글 찾을 수 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @PostMapping("/{idx}/application")
    public ResponseEntity<Void> createCareApplication(
            @RequestHeader(value = "Authorization") final String token,
            @ApiParam(value = "글 번호", required = true) @PathVariable Long idx,
            @RequestBody @Valid CareApplication careApplication,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.createCareApplication(careApplication, loginUser, idx);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "입양/임시보호 신청 승낙", notes = "입양/임시보호 글에 대한 신청을 승낙합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "승낙 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "관련 글/신청 찾을 수 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @CareTakerAuth
    @PostMapping("/application/{idx}/accept")
    public ResponseEntity<CareApplication> acceptCareApplication(
            @RequestHeader(value = "Authorization") final String token,
            @ApiParam(value = "신청 번호", required = true) @PathVariable Long idx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.acceptCareApplication(idx, loginUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
