package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.Cat;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(value = "MapController", description = "길냥이맵 관련 api")
@RestController
@RequestMapping("/api/maps")
public class ApiMapController {

    @PutMapping("/cat")
    public ResponseEntity putCat(@RequestBody final Cat cat){


        return ResponseEntity.status(HttpStatus.OK).body(c);
    }
}
