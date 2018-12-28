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
    @Auth
    @GetMapping
    public ResponseEntity<MarkerListDto> getMarkerList(@RequestHeader(value = "Authorization", required = true) final String header,
                                                       @RequestParam final Optional<Integer> emdcode){
        final Long userIdx = jwtService.decode(header).getIdx();

        return ResponseEntity.status(HttpStatus.OK).body(mapService.getMarkerListByRegion(mapService.getUser(userIdx), emdcode));
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @PostMapping
    public ResponseEntity requestMarkerRegisterOrEdit(
            @RequestHeader(value = "Authorization", required = true) final String header,
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
    @Auth
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDto>> getRegionList(@RequestHeader(value = "Authorization", required = true) final String header) {
        final Long userIdx = jwtService.decode(header).getIdx();

        return ResponseEntity.status(HttpStatus.OK).body(mapService.getRegionList(mapService.getUser(userIdx)));
    }

    //관리자용
    //요청조회
//    @GetMapping("/request")
//    public ResponseEntity getRequestList(@RequestHeader(value = "Authorization", required = false) final String header){
//        final Long userIdx = jwtService.decode(header).getIdx();
//
//        // 미승인, 승인, 거절
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
