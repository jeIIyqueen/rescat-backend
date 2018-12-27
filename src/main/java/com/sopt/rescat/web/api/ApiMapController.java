package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.MapRequestDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.utils.auth.Auth;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

//    @Auth
//    @PostMapping("/request/place/register")
//    public ResponseEntity requestPlaceRegister(
//            @RequestHeader(value = "Authorization", required = false) final String header,
//            @RequestBody MapRequestDto mapRequestDto){
//
//        final Long userIdx = jwtService.decode(header).getIdx();
//
//        // 등록
//
//        mapService.saveRequestPlaceRegister(userIdx, mapRequestDto);
//
//    }
//
//    @Auth
//    @PostMapping("/request/place/edit")
//    public ResponseEntity requestPlaceEdit(
//            @RequestHeader(value = "Authorization", required = false) final String header,
//            @RequestBody MapRequestDto mapRequestDto){
//
//        final Long userIdx = jwtService.decode(header).getIdx();
//
//        // 수정
//        mapService.
//    }
}
