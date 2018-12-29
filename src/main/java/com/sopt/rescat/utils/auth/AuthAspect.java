package com.sopt.rescat.utils.auth;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.UserRepository;
import com.sopt.rescat.service.JWTService;
import com.sopt.rescat.vo.JwtTokenVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

import javax.servlet.http.HttpServletRequest;

public class AuthAspect {
    private final static String AUTHORIZATION = "Authorization";

    /**
     * 실패 시 기본 반환 Response
     */

    private final HttpServletRequest httpServletRequest;

    private final JWTService jwtService;

    private final UserRepository userRepository;

    /**
     * Repository 의존성 주입
     */

    public AuthAspect(final HttpServletRequest httpServletRequest, final JWTService jwtService, final UserRepository userRepository) {
        this.httpServletRequest = httpServletRequest;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * 토큰 유효성 검사
     *
     * @param pjp
     * @return
     * @throws Throwable
     */

    //항상 @annotation 패키지 이름을 실제 사용할 annotation 경로로 맞춰줘야 한다.
    @Around("@annotation(com.sopt.rescat.utils.auth.Auth)")
    public Object around(final ProceedingJoinPoint pjp) throws Throwable {

        final String jwt = httpServletRequest.getHeader(AUTHORIZATION);

        //토큰 존재 여부 확인

        if (jwt == null) throw new UnAuthenticationException("token", "유효하지 않은 토큰입니다.");

        //토큰 해독

        final JwtTokenVO token = jwtService.decode(jwt);

        //토큰 검사

        if (token == null) {

            throw new UnAuthenticationException();

        } else {

            final User user = userRepository.findByIdx(token.getIdx());

            //유효 사용자 검사

            if (user == null) throw new UnAuthenticationException("token", "유효하지 않은 토큰입니다.");

            return pjp.proceed(pjp.getArgs());
        }
    }
}
