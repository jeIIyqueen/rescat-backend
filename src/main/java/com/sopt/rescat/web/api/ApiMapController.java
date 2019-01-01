package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.MarkerDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.utils.auth.AdminAuth;
import com.sopt.rescat.utils.auth.AuthAspect;
import com.sopt.rescat.utils.auth.CareTakerAuth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Api(value = "MapController", description = "길냥이맵 관련 api")
@RestController
@RequestMapping("/api/maps")
public class ApiMapController {

    private final JWTService jwtService;
    private final MapService mapService;

    public ApiMapController(JWTService jwtService, MapService mapService) {
        this.jwtService = jwtService;
        this.mapService = mapService;
    }

    @ApiOperation(value = "맵 마커 목록 전체 조회", notes = "유저가 선택한 지역과 해당 지역의 마커 목록을 반환합니다. 파라미터로 지역코드를 보내지 않으면 메인지역의 마커 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = MarkerDto.class),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "emdCode", value = "읍면동 지역코드", dataType = "integer")
    })
    @CareTakerAuth
    @GetMapping
    public ResponseEntity<List<MarkerDto>> getMarkerList(@RequestHeader(value = "Authorization") final String token,
                                                         @RequestParam final Optional<Integer> emdCode,
                                                         HttpServletRequest httpServletRequest) {
        User user = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);

        return ResponseEntity.status(HttpStatus.OK).body(mapService.getMarkerListByRegion(user, emdCode));
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @CareTakerAuth
    @PostMapping
    public ResponseEntity requestMarkerRegisterOrEdit(
            @RequestHeader(value = "Authorization") final String token,
            @RequestBody @Valid MapRequest mapRequest,
            HttpServletRequest httpServletRequest) throws IOException {
        User user = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        log.info(mapRequest.toString());

        mapService.saveMarkerRequest(user, mapRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @ApiOperation(value = "맵 마커 수정/등록 리스트 전체 조회", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = MapRequest.class),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @AdminAuth
    @GetMapping("/request")
    public ResponseEntity<List<MapRequest>> getAll(
            @RequestHeader(value = "Authorization") final String token,
            HttpServletRequest httpServletRequest) {
        User user = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        return ResponseEntity.status(HttpStatus.OK).body(mapService.getAllMapRequest());
    }


    @ApiOperation(value = "맵 마커 수정/등록 요청 승인", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 승인하고 저장합니다.")
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
    @PostMapping("/request/{mapRequestIdx}/approval")
    public ResponseEntity approveMapRequest(
            @RequestHeader(value = "Authorization") final String token,
            @PathVariable final Long mapRequestIdx){
        return ResponseEntity.status(HttpStatus.OK).body(mapService.approveMapRequest(mapRequestIdx));
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청 거절", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "거절 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "요청 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @AdminAuth
    @PostMapping("/request/{mapRequestIdx}/refusal")
    public ResponseEntity refuseMapRequest(
            @RequestHeader(value = "Authorization") final String token,
            @PathVariable final Long mapRequestIdx) {
        return ResponseEntity.status(HttpStatus.OK).body(mapService.refuseMapRequest(mapRequestIdx));
    }
}

