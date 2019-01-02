package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.MapService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.auth.Auth;
import com.sopt.rescat.utils.auth.AuthAspect;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;


@Slf4j
@Api(value = "UserController", description = "유저 관련 api")
@RestController
@RequestMapping("/api/users")
public class ApiUserController {
    private final static String PHONE_REX = "^01([0|1|6|7|8|9]?)-?([0-9]{3,4})-?([0-9]{4})$";

    private final UserService userService;
    private final JWTService jwtService;
    private final MapService mapService;

    public ApiUserController(final UserService userService, final JWTService jwtService, final MapService mapService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.mapService = mapService;
    }

    @ApiOperation(value = "일반 유저 생성", notes = "일반 유저를 생성합니다. 성공시 jwt 토큰을 바디에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "유저 생성 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러", response = ExceptionDto.class),
            @ApiResponse(code = 409, message = "아이디 중복 또는 닉네임 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("")
    public ResponseEntity<JwtTokenDto> join(@RequestBody @Valid UserJoinDto userJoinDto) {
        User newUser = userService.create(userJoinDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(JwtTokenDto.builder().token(jwtService.create(newUser.getIdx())).build());
    }

    @ApiOperation(value = "아이디 중복 검사", notes = "유저가 입력한 아이디에 대해 중복을 검사합니다. 중복이 없을 시 true를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "아이디 사용 가능", response = Boolean.class),
            @ApiResponse(code = 409, message = "아이디 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/duplicate/id")
    public ResponseEntity<Boolean> checkIdDuplication(@RequestParam String id) {
        return ResponseEntity.status(HttpStatus.OK).body(!userService.isExistingId(id));
    }

    @ApiOperation(value = "닉네임 중복 검사", notes = "유저가 입력한 닉네임에 대해 중복을 검사합니다. 중복이 없을 시 true를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "닉네임 사용 가능", response = Boolean.class),
            @ApiResponse(code = 409, message = "닉네임 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/duplicate/nickname")
    public ResponseEntity<Boolean> checkNicknameDuplication(@RequestParam String nickname) {
        return ResponseEntity.status(HttpStatus.OK).body(!userService.isExistingNickname(nickname));
    }

    @ApiOperation(value = "유저 로그인", notes = "유저가 로그인합니다. 성공시 jwt 토큰을 바디에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "로그인 성공"),
            @ApiResponse(code = 401, message = "로그인 실패", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.status(HttpStatus.OK).body(JwtTokenDto.builder().token(jwtService.create(userService.login(userLoginDto).getIdx())).build());
    }

    @ApiOperation(value = "핸드폰 인증", notes = "핸드폰 번호를 받아 문자를 보내고, 해당 인증코드를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "인증 성공", response = AuthenticationCodeVO.class),
            @ApiResponse(code = 400, message = "잘못된 번호", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러"),
            @ApiResponse(code = 501, message = "문자보내기 실패")
    })
    @PostMapping("/authentications/phone")
    public ResponseEntity<AuthenticationCodeVO> authenticatePhone(
            @ApiParam(value = "01000000000 또는 010-0000-0000", required = true)
            @Valid
            @Pattern(regexp = PHONE_REX, message = "핸드폰번호는 000-0000-0000 또는 00000000000 형식이어야 합니다.")
            @RequestParam String phone) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.sendSms(phone));
    }


    @ApiOperation(value = "케어테이커 인증 요청", notes = "케어테이커 인증을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @PostMapping("/authentications/caretaker")
    public ResponseEntity requestCareTaker(
            @RequestBody @Valid CareTakerRequest careTakerRequest,
            HttpServletRequest httpServletRequest) throws IOException {
        User user = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);

        userService.saveCareTakerRequest(user, careTakerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "유저의 마이페이지 조회", notes = "유저의 마이페이지 목록(아이디, 닉네임, 롤, 지역)을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @GetMapping("/mypage")
    public ResponseEntity<UserMypageDto> getMypage(@RequestHeader(value = "Authorization") final String token,
                                                   HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserMypage(loginUser));
    }

    @ApiOperation(value = "케어테이커 유저의 지역 목록 조회", notes = "케어테이커 유저가 인증한 지역 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @GetMapping("/mypage/regions")
    public ResponseEntity<List<RegionDto>> getRegionList(@RequestHeader(value = "Authorization") final String token,
                                                         HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        return ResponseEntity.status(HttpStatus.OK).body(userService.getRegionList(loginUser));
    }
}
