package com.sopt.rescat.security;

import com.fasterxml.jackson.core.JsonParseException;
import com.sopt.rescat.dto.ExceptionDto;
import com.sopt.rescat.error.ErrorResponse;
import com.sopt.rescat.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
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

    @ExceptionHandler(FailureException.class)
    public ResponseEntity<ErrorResponse> failure(Exception exception) {
        log.debug("FailureException is happened!");
        return new ResponseEntity<>(ErrorResponse.ofString(exception.getMessage()), HttpStatus.NOT_IMPLEMENTED);
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ExceptionDto>> constraintViolationException(ConstraintViolationException exception) {
        List<ExceptionDto> exceptionDtos = new ArrayList<>();
        exception.getConstraintViolations()
                .forEach(constraintViolation -> exceptionDtos.add(buildExceptionDto(constraintViolation.getMessage(), constraintViolation.getInvalidValue().toString())));

        return new ResponseEntity(exceptionDtos, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotExistException.class)
    public ResponseEntity<ExceptionDto> notExist(NotExistException exception) {
        log.debug("NotExistException is happened!");
        return new ResponseEntity<>(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ExceptionDto> invalidValue(InvalidValueException exception) {
        log.debug("InvalidValueException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionDto> notFound(NotFoundException exception) {
        log.debug("NotFoundException is happened!");
        return new ResponseEntity(ExceptionDto.toExceptionDto(exception.getField(), exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadable(HttpMessageNotReadableException exception) {
        log.debug("HttpMessageNotReadableException is happened!");
        return new ResponseEntity<>(ErrorResponse.ofString(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    public ExceptionDto buildExceptionDto(String message, String field) {
        return ExceptionDto.builder()
                .message(message)
                .field(field)
                .build();
    }
}
