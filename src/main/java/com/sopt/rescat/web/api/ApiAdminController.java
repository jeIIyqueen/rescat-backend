package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.response.BannerDto;
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
import java.util.List;

@Api(value = "ApiAdminController", description = "관리자페이지 관련 api")
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {
    private UserService userService;
    private MapService mapService;

    public ApiAdminController(final UserService userService, final MapService mapService) {
        this.userService = userService;
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
            @ApiResponse(code = 200, message = "케어테이커 인증요청 승인/거절 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "status", value = "1: 승인, 2: 거절", required = true, dataType = "integer", paramType = "body")
    })
    @AdminAuth
    @PostMapping("/care-taker-requests/{idx}")
    public ResponseEntity<Void> approveCareTaker(
            @PathVariable Long idx,
            @RequestBody @Range(min = 1, max = 2) Integer status,
            HttpServletRequest httpServletRequest
    ) {
        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        userService.approveCareTaker(idx, status, approver);
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
    public ResponseEntity<Iterable<MapRequest>> getAll(
            @RequestHeader(value = "Authorization") final String token,
            HttpServletRequest httpServletRequest) {
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
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "status", value = "1: 승인, 2: 거절", required = true, dataType = "integer", paramType = "body")
    })
    @AdminAuth
    @PostMapping("/map-request/{idx}")
    public ResponseEntity approveMapRequest(
            @RequestHeader(value = "Authorization") final String token,
            @PathVariable Long idx,
            @RequestBody @Range(min = 1, max = 2) Integer status,
            HttpServletRequest httpServletRequest){
        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        mapService.approveMap(idx, status, approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
