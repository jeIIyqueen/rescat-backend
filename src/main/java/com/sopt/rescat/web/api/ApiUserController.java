package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.ExceptionDto;
import com.sopt.rescat.dto.JwtTokenDto;
import com.sopt.rescat.dto.UserJoinDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.AlreadyExistsException;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.ws.Response;
import java.util.Map;

import java.net.URI;

import static com.sopt.rescat.utils.HttpSessionUtils.getUserFromSession;

@Slf4j
@Api(value = "UserController", description = "유저 관련 api")
@RestController
@RequestMapping("/api/users")
public class ApiUserController {
    private final UserService userService;
    private final JWTService jwtService;

    public ApiUserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }


    @ApiOperation(value = "일반 유저 생성", notes = "일반 유저를 생성합니다. 성공시 jwt 토큰을 헤더에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "유저 생성 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러", response = ExceptionDto.class),
            @ApiResponse(code = 409, message = "아이디 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("")
    public ResponseEntity<JwtTokenDto> join(@RequestBody @Valid UserJoinDto userJoinDto) {
        User newUser = userService.create(userJoinDto);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwtService.create(newUser.getIdx()));
        return ResponseEntity.status(HttpStatus.CREATED).headers(httpHeaders).build();
    }

    @ApiOperation(value = "아이디 중복 검사", notes = "유저가 입력한 아이디에 대해 중복을 검사합니다. 중복이 없을 시 true를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "아이디 사용 가능", response = Boolean.class),
            @ApiResponse(code = 409, message = "아이디 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/duplicate/{id}")
    public ResponseEntity<Boolean> checkIdDuplicate(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(!userService.isExistingId(id));
    }

    @ApiOperation(value = "유저 로그인", notes = "유저가 로그인합니다. 성공시 jwt 토큰을 헤더에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "로그인 성공"),
            @ApiResponse(code = 401, message = "로그인 실패", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody UserLoginDto userLoginDto) {
       JwtTokenDto jwtTokenDto = new JwtTokenDto(jwtService.create(userService.login(userLoginDto).getIdx()));
       HttpHeaders httpHeaders = new HttpHeaders();
       httpHeaders.add("Authorization",jwtTokenDto.getToken());
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).build();
    }

    @PostMapping("/authentications/{phone}")
    public ResponseEntity<AuthenticationCodeVO> authenticatePhone(@PathVariable String phone) {
        log.debug("authenticatePhone 시작", phone);
        return ResponseEntity.status(HttpStatus.OK).body(userService.sendMms(phone));
    }
}
