package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.FundingComment;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.request.FundingRequestDto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.service.FundingService;
import com.sopt.rescat.utils.auth.Auth;
import com.sopt.rescat.utils.auth.AuthAspect;
import com.sopt.rescat.utils.auth.CareTakerAuth;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(value = "ApiFundingController", description = "크라우드 펀딩 api")
@RestController
@RequestMapping("/api/fundings")
public class ApiFundingController {
    private FundingService fundingService;

    public ApiFundingController(final FundingService fundingService) {
        this.fundingService = fundingService;
    }

    @ApiOperation(value = "치료비 모금/ 프로젝트 모금 리스트 조회", notes = "category에 따라 펀딩 글 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "치료비 모금/ 프로젝트 모금 리스트 반환 성공", response = FundingResponseDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<FundingResponseDto>> getAllBy(
            @ApiParam(value = "0: 치료비 모금, 1: 프로젝트", required = true)
            @RequestParam Integer category) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findAllBy(category));
    }

    @ApiOperation(value = "크라우드 펀딩 글 등록", notes = "크라우드 펀딩 글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 등록 성공"),
            @ApiResponse(code = 400, message = "파라미터 형식 오류"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @CareTakerAuth
    @PostMapping("")
    public ResponseEntity<Void> create(
            @RequestHeader(value = "Authorization") final String token,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FundingRequestDto fundingRequestDto) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        fundingService.create(fundingRequestDto, loginUser);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "크라우드 펀딩 글 조회", notes = "idx 에 따른 크라우드 펀딩 글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 반환 성공", response = Funding.class),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}")
    public ResponseEntity<Funding> getFundingByIdx(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findBy(idx));
    }

    @ApiOperation(value = "크라우드 펀딩 글의 댓글 조회", notes = "idx 에 따른 크라우드 펀딩 글의 댓글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글의 댓글 리스트 반환 성공", response = FundingComment.class),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/{idx}/comments")
    public ResponseEntity<Iterable<FundingComment>> getComments(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.findCommentsBy(idx));
    }

    @ApiOperation(value = "크라우드 펀딩 글의 댓글 등록", notes = "idx 에 따른 크라우드 펀딩 글의 댓글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "크라우드 펀딩 글의 댓글 등록 성공", response = FundingComment.class),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "댓글 작성 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @PostMapping("/{idx}/comments")
    @Auth
    public ResponseEntity<FundingComment> createComment(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx,
            @RequestBody FundingComment fundingComment,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        return ResponseEntity.status(HttpStatus.CREATED).body(fundingService.createComment(idx, fundingComment, loginUser));
    }

    @ApiOperation(value = "크라우드 펀딩 글의 댓글 삭제", notes = "idx 에 따른 크라우드 펀딩 글의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "크라우드 펀딩 글의 댓글 삭제 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "댓글 삭제 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "idx", value = "fundingIdx", required = true, dataType = "long", paramType = "path")
    })
    @DeleteMapping("/{idx}/comments/{comment-idx}")
    @Auth
    public ResponseEntity<Void> createComment(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable(name = "comment-idx") Long commentIdx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        fundingService.deleteComment(commentIdx, loginUser);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "마일리지 결제", notes = "idx에 해당하는 펀딩 글에 마일리지로 결제합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "마일리지 결제 성공"),
            @ApiResponse(code = 400, message = "idx 에 해당하는 글 없음 / 마일리지가 부족함"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @PostMapping("/{idx}/pay")
    public ResponseEntity<Void> payForMileage(
            @RequestHeader(value = "Authorization") final String token,
            @PathVariable Long idx,
            @RequestBody Long mileage,
            HttpServletRequest httpServletRequest) {
        if (mileage <= 0) throw new InvalidValueException("mileage", "mileage 값은 음수일 수 없습니다.");

        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        fundingService.payForMileage(idx, mileage, loginUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "펀딩 글 4개 리스트", notes = "펀딩 글 4개 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "펀딩 글 4개 리스트 반환 성공", response = FundingResponseDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/main")
    public ResponseEntity<Iterable<FundingResponseDto>> get4Fundings() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.find4Fundings());
    }
}
