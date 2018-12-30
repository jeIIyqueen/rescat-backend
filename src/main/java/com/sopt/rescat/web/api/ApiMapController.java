package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.MapRequestDto;
import com.sopt.rescat.dto.MarkerListDto;
import com.sopt.rescat.dto.RegionDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.utils.auth.Auth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @ApiResponse(code = 200, message = "조회 성공", response = MarkerListDto.class),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "emdCode", value = "읍면동 지역코드", required = false, dataType = "string", paramType = "body", defaultValue = "")
    })
    @Auth
    @GetMapping
    public ResponseEntity<MarkerListDto> getMarkerList(@RequestHeader(value = "Authorization") final String header,
                                                       @RequestParam final Optional<Integer> emdCode){
        final Long userIdx = jwtService.decode(header).getIdx();

        return ResponseEntity.status(HttpStatus.OK).body(mapService.getMarkerListByRegion(mapService.getUser(userIdx), emdCode));
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "registerType", value = "요청 유형(0: 등록, 1: 수정)", required = true, dataType = "integer", paramType = "body"),
            @ApiImplicitParam(name = "requestType", value = "마커 유형(0: 배식소, 1: 병원, 2: 길고양이)", required = true, dataType = "integer", paramType = "body"),
            @ApiImplicitParam(name = "name", value = "고양이 이름(10자이내) 또는 배식소, 병원 이름(50자이내)", required = true, dataType = "string", paramType = "body"),
            @ApiImplicitParam(name = "etc", value = "특징 또는 부가정보", required = true, dataType = "string", paramType = "body"),
            @ApiImplicitParam(name = "lat", value = "위도 좌표", required = true, dataType = "float", paramType = "body"),
            @ApiImplicitParam(name = "lng", value = "경도 좌표", required = true, dataType = "float", paramType = "body"),
            @ApiImplicitParam(name = "lng", value = "(only병원)주소", required = true, dataType = "float", paramType = "body"),
            @ApiImplicitParam(name = "photo", value = "사진 파일", required = true, dataType = "MultipartFile", paramType = "body"),
            @ApiImplicitParam(name = "radius", value = "(only길고양이)활동반경", required = true, dataType = "string", paramType = "body"),
            @ApiImplicitParam(name = "sex", value = "(only길고양이)성별(0: 남, 1: 여)", required = true, dataType = "integer", paramType = "body"),
            @ApiImplicitParam(name = "age", value = "(only길고양이)추정나이", required = true, dataType = "string", paramType = "body"),
            @ApiImplicitParam(name = "tnr", value = "(only길고양이)중성화여부(0: 미완료, 1: 완료)", required = true, dataType = "string", paramType = "body")
    })
    @Auth
    @PostMapping
    public ResponseEntity requestMarkerRegisterOrEdit(
            @RequestHeader(value = "Authorization") final String header,
            MapRequestDto mapRequestDto) throws IOException {
        final Long userIdx = jwtService.decode(header).getIdx();
        log.info(mapRequestDto.toString());

        mapService.saveMarkerRequest(mapService.getUser(userIdx), mapRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "유저의 지역 목록 조회", notes = "유저가 인증한 지역 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDto>> getRegionList(@RequestHeader(value = "Authorization") final String header) {
        final Long userIdx = jwtService.decode(header).getIdx();

        return ResponseEntity.status(HttpStatus.OK).body(mapService.getRegionList(mapService.getUser(userIdx)));
    }

    //관리자용
    //요청조회
//    @GetMapping("/request")
//    public ResponseEntity getRequestList(@RequestHeader(value = "Authorization", required = false) final String header){
//        final Long userIdx = jwtService.decode(header).getIdx();
//
//        // 보류, 승인, 거절
//
//        return
//    }
//
//    //요청저장
//    @PostMapping("/request/{mapRequestIdx}")
//    public ResponseEntity createMarker(
//            @RequestHeader(value = "Authorization", required = false) final String header,
//            @PathVariable final Long mapRequestIdx){
//
//    }
//
//    //수정
//    @PutMapping("request/{mapRequestIdx}")
//    public ResponseEntity editMarker(
//            @RequestHeader(value = "Authorization", required = false) final String header,
//            @PathVariable final Long mapRequestIdx){
//
//    }
//
//    //삭제



}
