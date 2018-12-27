package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.MapRequestDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.utils.auth.Auth;
import com.sopt.rescat.vo.JwtTokenVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(value = "MapController", description = "길냥이맵 관련 api")
@RestController
@RequestMapping("/api/maps")
public class ApiMapController {

    private final MapService mapService;
    private final JWTService jwtService;

    public ApiMapController (final MapService mapService,final JWTService jwtService) {
        this.mapService = mapService;
        this.jwtService = jwtService;
    }

    @Auth
    @PostMapping("cat/register")
    public ResponseEntity requestCatRegister(@RequestHeader(value = "Authorization") final String header,
                                             MapRequestDto mapRequestDto){
        return ResponseEntity.status(HttpStatus.OK).body(mapService.saveCatRequest(jwtService.decode(header).getIdx(),mapRequestDto));
    }

//    @Auth
//    @PostMapping("/cat/edit")
//    public ResponseEntity editCat(@RequestBody User user, MapRequestDto mapRequestDto){
//        mapRequestDto.builder().birth(user.getIdx(), mapRequestDto);
//
//        mapService.saveCatRequest(user.getIdx(),mapRequestDto);
//
//       return ResponseEntity.status(HttpStatus.OK).body(c);
//    }
}
