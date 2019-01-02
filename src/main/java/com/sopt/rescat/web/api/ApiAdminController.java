package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.service.CarePostService;
import com.sopt.rescat.service.FundingService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.auth.AdminAuth;
import com.sopt.rescat.utils.auth.AuthAspect;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(value = "ApiAdminController", description = "관리자페이지 관련 api")
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {
    private UserService userService;
    private FundingService fundingService;
    private CarePostService carePostService;
    private MapService mapService;


    public ApiAdminController(final UserService userService,
                              final FundingService fundingService,
                              final CarePostService carePostService,
                              final MapService mapService) {
        this.userService = userService;
        this.fundingService = fundingService;
        this.carePostService = carePostService;
        this.mapService = mapService;
    }

    @ApiOperation(value = "케어테이커 인증요청 리스트 api", notes = "케어테이커 인증요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "케어테이커 인증요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @GetMapping("/care-taker-requests")
    public ResponseEntity<Iterable<CareTakerRequest>> showCareTakerRequest() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getCareTakerRequest());
    }

    @ApiOperation(value = "케어테이커 인증요청 승인 api", notes = "케어테이커 인증요청을 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "케어테이커 인증요청 처리 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
    })
    @AdminAuth
    @PostMapping("/care-taker-requests/{idx}")
    public ResponseEntity<Void> approveCareTaker(
            @PathVariable Long idx,
            @ApiParam(value = "{'status': 1}", example = "1: 승인, 2: 거절")
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpServletRequest) {
        if(!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        userService.approveCareTaker(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "크라우드 펀딩 글 게시요청 리스트 api", notes = "크라우드 펀딩 글 게시요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 게시요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @GetMapping("/funding-requests")
    public ResponseEntity showFundingRequest(){
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.getFundingRequests());
    }

    @ApiOperation(value = "크라우드 펀딩 글 게시 승인 api", notes = "idx 에 따른 크라우드 펀딩 글 게시를 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 게시요청 처리 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @PutMapping("/funding-requests/{idx}")
    public ResponseEntity<Void> confirmFundingPost(
            @PathVariable Long idx,
            @ApiParam(value = "{'status':1}", example = "1: 승인, 2: 거절")
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpServletRequest) {
        if(!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        fundingService.confirmFunding(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @ApiOperation(value = "입양/임시보호 글 게시요청 리스트 api", notes = "입양/임시보호 글 게시요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 게시요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @GetMapping("/care-post-requests")
    public ResponseEntity showCarePostRequest(){
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.getCarePostRequests());
    }

    @ApiOperation(value = "입양/임시보호 글 게시 api", notes = "idx 에 따른 입양/임시보호 글 게시를 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 게시요청 처리 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @PutMapping("/care-post-requests/{idx}")
    public ResponseEntity<Void> confirmCarePost(
            @PathVariable Long idx,
            @ApiParam(value = "{'status': 1}", example = "1: 승인, 2: 거절")
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpServletRequest) {
        if(!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.confirmCarePost(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @ApiOperation(value = "맵 마커 수정/등록 리스트 조회", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = MapRequest.class),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @AdminAuth
    @GetMapping("/map-request")
    public ResponseEntity<Iterable<MapRequest>> showMapRequest() {
        return ResponseEntity.status(HttpStatus.OK).body(mapService.getMapRequest());
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청 승인/거절", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "승인 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "요청 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @AdminAuth
    @PostMapping("/map-request/{idx}")
    public ResponseEntity approveMapRequest(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpServletRequest){
        if(!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        mapService.approveMap(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
