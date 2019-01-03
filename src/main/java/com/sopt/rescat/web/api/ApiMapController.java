package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.MarkerDto;
import com.sopt.rescat.service.MapService;
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
    private final MapService mapService;

    public ApiMapController(MapService mapService) {
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
    public ResponseEntity<List<MarkerDto>> getMarkerList(
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
            @RequestBody @Valid MapRequest mapRequest,
            HttpServletRequest httpServletRequest) throws IOException {
        User user = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);

        mapService.saveMarkerRequest(user, mapRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}