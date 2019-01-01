package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.auth.AdminAuth;
import com.sopt.rescat.utils.auth.AuthAspect;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(value = "ApiAdminController", description = "관리자페이지 관련 api")
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {
    private UserService userService;

    public ApiAdminController(final UserService userService) {
        this.userService = userService;
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
            @ApiResponse(code = 200, message = "케어테이커 인증요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @AdminAuth
    @PostMapping("/care-taker-requests/{idx}")
    public ResponseEntity<Void> approveCareTaker(
            @PathVariable Long idx,
            @ApiParam(value = "status", example = "1: 승인, 2: 거절")
            @RequestBody @Range(min = 1, max = 2) Integer status,
            HttpServletRequest httpServletRequest
    ) {
        User approver = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        userService.approveCareTaker(idx, status, approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
