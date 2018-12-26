package com.sopt.rescat.service;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.UserJoinDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.AlreadyExistingException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean decodePassword(String password) {
        return passwordEncoder.matches("rescat", password);
    }
}
