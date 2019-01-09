package com.sopt.rescat.web.api;

import com.sopt.rescat.dto.PhotoDto;
import com.sopt.rescat.service.S3FileService;
import com.sopt.rescat.utils.auth.Auth;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*")
@Slf4j
@Api(value = "ApiPhotoController", description = "사진 api")
@RestController
@RequestMapping("/api/photo")
public class ApiPhotoController {
    private final S3FileService s3FileService;

    public ApiPhotoController(final S3FileService s3FileService) {
        this.s3FileService = s3FileService;
    }

    @ApiOperation(value = "사진 업로드", notes = "S3에 사진을 업로합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "저장 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 413, message = "사진 크기 제한 초과"),
            @ApiResponse(code = 500, message = "서버 에러"),
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @PostMapping()
    public ResponseEntity<PhotoDto> uploadPhoto(@RequestParam("data") MultipartFile multipartFile) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(new PhotoDto(s3FileService.upload(multipartFile, "static")));
    }

    @ApiOperation(value = "사진 삭제", notes = "s3에 있는 사진을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "삭제 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @DeleteMapping()
    public ResponseEntity<Void> removePhoto(@RequestParam("photoUrl") String photoUrl) {
        s3FileService.remove(photoUrl);
        return ResponseEntity.ok().build();
    }
}
