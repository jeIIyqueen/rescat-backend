package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.JwtTokenDto;
import com.sopt.rescat.dto.UserJoinDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.service.UserService;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlrpc.client.util.ClientFactory;
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
@RestController
@RequestMapping("/api/users")
public class ApiUserController {
    private final UserService userService;
    private final JWTService jwtService;

    public ApiUserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }


    @PostMapping("")
    public ResponseEntity<User> join(@RequestBody @Valid UserJoinDto userJoinDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userJoinDto));
    }

    @PostMapping("/id/duplicate")
    public ResponseEntity<Boolean> checkDuplicateId(@RequestBody Map<String, String> param) {
        return ResponseEntity.status(HttpStatus.OK).body(!userService.isExistingId(param.get("id")));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody UserLoginDto userLoginDto) {
       JwtTokenDto jwtTokenDto = new JwtTokenDto(jwtService.create(userService.login(userLoginDto).getIdx()));
        return ResponseEntity.status(HttpStatus.OK).body(jwtTokenDto);

    }

    @PostMapping("/authentications/{phone}")
    public ResponseEntity<AuthenticationCodeVO> authenticatePhone(@PathVariable String phone) {
        log.debug("authenticatePhone 시작", phone);
        return ResponseEntity.status(HttpStatus.OK).body(userService.sendMms(phone));
    }
}
