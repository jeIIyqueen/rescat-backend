package com.sopt.rescat.security;

import com.sopt.rescat.dto.ExceptionDto;
import com.sopt.rescat.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class SecurityControllerAdvice {
    @ExceptionHandler(NotMatchException.class)
    public ResponseEntity<ExceptionDto> notMatch(NotMatchException exception) {
        log.debug("NotMatchException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnAuthenticationException.class)
    public ResponseEntity<ExceptionDto> unAuthentication(UnAuthenticationException exception) {
        log.debug("UnAuthenticationException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ExceptionDto> alreadyExists(AlreadyExistsException exception) {
        log.debug("AlreadyExistsException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ExceptionDto>> invalidMethodArgument(MethodArgumentNotValidException exception) {
        log.debug("[MethodArgumentNotValidException] {}", exception.getBindingResult().getAllErrors());
        List<ExceptionDto> exceptionDtos = new ArrayList<>();

        exception.getBindingResult().getAllErrors()
                .forEach(validError -> exceptionDtos.add(ExceptionDto.toExceptionDto(validError)));

        return new ResponseEntity(exceptionDtos, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ExceptionDto> invalidValue(InvalidValueException exception) {
        log.debug("InvalidValueException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionDto> invalidValue(NotFoundException exception) {
        log.debug("NotFoundException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.NOT_FOUND);
    }
}
