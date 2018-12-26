package com.sopt.rescat.security;

import com.sopt.rescat.error.ErrorResponse;
import com.sopt.rescat.exception.AlreadyExistingException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityControllerAdvice {
    @ExceptionHandler(NotMatchException.class)
    public ResponseEntity<ErrorResponse> notMatch(Exception exception) {
        log.debug("NotMatchException is happened!");
        return new ResponseEntity<>(ErrorResponse.ofString(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnAuthenticationException.class)
    public ResponseEntity<ErrorResponse> unAuthentication(Exception exception) {
        log.debug("UnAuthenticationException is happened!");
        return new ResponseEntity<>(ErrorResponse.ofString(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AlreadyExistingException.class)
    public ResponseEntity<ErrorResponse> alreadyExisting(Exception exception) {
        log.debug("AlreadyExistingException is happened!");
        return new ResponseEntity<>(ErrorResponse.ofString(exception.getMessage()), HttpStatus.CONFLICT);
    }
}
