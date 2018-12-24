package com.sopt.rescat.service;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
