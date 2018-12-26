package com.sopt.rescat.service;

import com.sopt.rescat.domain.User;

import com.sopt.rescat.dto.UserJoinDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.AlreadyExistingException;
import com.sopt.rescat.exception.FailureException;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.UserRepository;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiClass;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiResult;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,final JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Boolean isExistingId(String id) {
        if(userRepository.findById(id).isPresent()) {
            throw new AlreadyExistingException("이미 사용중인 아이디입니다.");
        }
        return Boolean.FALSE;
    }

    public User create(UserJoinDto userJoinDto) {
        isExistingId(userJoinDto.getId());
        return userRepository.save(userJoinDto.toUser(passwordEncoder.encode(userJoinDto.getPassword())));
    }

    public User login(UserLoginDto userLoginDto) {
        User savedUser = userRepository.findById(userLoginDto.getId())
                .orElseThrow(() -> new UnAuthenticationException("해당 ID를 가진 사용자가 존재하지 않습니다."));
        savedUser.matchPasswordBy(userLoginDto, passwordEncoder);
        return savedUser;
    }

    public AuthenticationCodeVO sendMms(String phone) {
        int randomCode = getRandomCode();
        String arr[] = {
                "sms",
                "rescat",
                "rescat 입니다.",                             // 제목
                "고객님의 인증번호는 " + randomCode + " 입니다.",
                "01040908370",                              // 발신번호
                phone,                                      // 수신번호
                "0"                                         // 즉시발송
        };

        ApiClass api = new ApiClass();
        ApiResult res = api.getResult(api.send(arr));
        if(res.getCode().equals("0000")) {
            return new AuthenticationCodeVO(randomCode);
        }
        throw new FailureException("문자 발송을 실패했습니다.");
    }

    private int getRandomCode() {
        return (int) Math.floor(Math.random() * 1000000);
    }
}
