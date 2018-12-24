package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.JwtTokenDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.utils.HttpSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class ApiUserController {
    private final UserService userService;
    private final JWTService jwtService;

    public ApiUserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody UserLoginDto userLoginDto, HttpSession httpSession) {
        // 1. 세션에 토큰 저장
        HttpSessionUtils.setTokenInSession(
                httpSession,
                JwtTokenDto.builder()
                        .token(jwtService.create(userService.login(userLoginDto).getIdx()))
                        .build()
                        .getToken()
        );
        // 2. OK 인 상태메세지를 클라이언트에게 보냄
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
