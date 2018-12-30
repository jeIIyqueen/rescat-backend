package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.auth.Auth;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import javax.validation.constraints.Pattern;

@Slf4j
@Api(value = "UserController", description = "유저 관련 api")
@RestController
@RequestMapping("/api/users")
@Validated
public class ApiUserController {
    private final static String PHONE_REX = "^01([0|1|6|7|8|9]?)-?([0-9]{3,4})-?([0-9]{4})$";
    private final UserService userService;
    private final JWTService jwtService;

    public ApiUserController(final UserService userService, final JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }


    @ApiOperation(value = "일반 유저 생성", notes = "일반 유저를 생성합니다. 성공시 jwt 토큰을 헤더에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "유저 생성 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러", response = ExceptionDto.class),
            @ApiResponse(code = 409, message = "아이디 중복 또는 닉네임 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("")
    public ResponseEntity<Void> join(@RequestBody @Valid UserJoinDto userJoinDto) {
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

    @ApiOperation(value = "닉네임 중복 검사", notes = "유저가 입력한 닉네임에 대해 중복을 검사합니다. 중복이 없을 시 true를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "닉네임 사용 가능", response = Boolean.class),
            @ApiResponse(code = 409, message = "닉네임 중복", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/duplicate/{nickname}")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@PathVariable String nickname) {
        return ResponseEntity.status(HttpStatus.OK).body(!userService.isExistingNickname(nickname));
    }

    @ApiOperation(value = "유저 로그인", notes = "유저가 로그인합니다. 성공시 jwt 토큰을 헤더에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "로그인 성공"),
            @ApiResponse(code = 401, message = "로그인 실패", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody UserLoginDto userLoginDto) {
        JwtTokenDto jwtTokenDto = new JwtTokenDto(jwtService.create(userService.login(userLoginDto).getIdx()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", jwtTokenDto.getToken());
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).build();
    }

    @ApiOperation(value = "핸드폰 인증", notes = "핸드폰 번호를 받아 문자를 보내고, 해당 인증코드를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "인증 성공", response = AuthenticationCodeVO.class),
            @ApiResponse(code = 400, message = "잘못된 번호", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러"),
            @ApiResponse(code = 501, message = "문자보내기 실패")
    })
    @PostMapping("/authentications/{phone}")
    public ResponseEntity<AuthenticationCodeVO> authenticatePhone(
            @ApiParam(value = "01000000000 또는 010-0000-0000", required = true)
            @Valid
            @Pattern(regexp = PHONE_REX, message = "핸드폰번호는 000-0000-0000 또는 00000000000 형식이어야 합니다.")
            @PathVariable String phone) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.sendSms(phone));
    }


    @ApiOperation(value = "토큰으로 유저 조회", notes = "토큰으로 해당 유저를 조회합니다. 성공시 해당 유저를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @GetMapping("/authentications/{idx}")
    public ResponseEntity<User> authenticateUserIdx(@RequestHeader("Authorization") final String header,
                                                    @PathVariable("idx") final long idx) {
        User getUser = userService.findByUserIdx(idx);
        if(jwtService.decode(header).getIdx() == idx) {
            return ResponseEntity.status(HttpStatus.OK).body(getUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ApiOperation(value = "케어테이커 인증 요청", notes = "케어테이커 인증을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "요청 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @PostMapping("/caretaker")
    public ResponseEntity requestCareTaker(@RequestHeader(value = "Authorization") final String header,
                                                             CareTakerRequestDto careTakerRequestDto) throws IOException {
        final Long userIdx = jwtService.decode(header).getIdx();

        userService.saveCareTakerRequest(userIdx, careTakerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


    //마이페이지 첫화면(회원조회), (id,닉네임,포토,롤,지역3개)
    @ApiOperation(value = "유저의 마이페이지", notes = "유저의 마이페이지 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @GetMapping("/mypage")
    public ResponseEntity<UserMypageDto> getMypage(@RequestHeader("Authorization") final String header) {
        final Long userIdx = jwtService.decode(header).getIdx();
        User user = userService.findByUserIdx(userIdx);
        UserMypageDto userMypageDto = new UserMypageDto(user);
        return ResponseEntity.status(HttpStatus.OK).body(userMypageDto);
    }


//    @ApiOperation(value = "유저의 지역 목록 조회", notes = "유저가 인증한 지역 목록을 반환합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "조회 성공"),
//            @ApiResponse(code = 401, message = "권한 없음"),
//            @ApiResponse(code = 500, message = "서버 에러")
//    })
//    @Auth
//    @GetMapping("/mypage/regions")
//    public ResponseEntity<List<RegionDto>> getRegionList(@RequestHeader(value = "Authorization") final String header) {
//        final Long userIdx = jwtService.decode(header).getIdx();
//        return ResponseEntity.status(HttpStatus.OK).body(userService.getRegionList(userService.findByUserIdx(userIdx)));
//    }

    //지역 수정
//    @Auth
//    @PutMapping("/mypage/regions/edit")
//    public ResponseEntity<List<RegionDto>> updateRegionList(@RequestHeader(value = "Authorization") final String header) {
////        final Long userIdx = jwtService.decode(header).getIdx();
////        return ResponseEntity.status(HttpStatus.OK).body(userService.getRegionList(userService.findByUserIdx(userIdx)));
//    }

    //회원 정보 수정 //일반회원,,
//    @Auth
//    @PutMapping("/mypage/edit")
//    public ResponseEntity<Void> updateUser(@RequestHeader(value = "Authorization") final String header,
//                                           @RequestBody @Valid UserJoinDto userJoinDto) {
//        userService.create(userJoinDto);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }


}
