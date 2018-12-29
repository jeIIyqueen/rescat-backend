package com.sopt.rescat.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sopt.rescat.vo.JwtTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static com.auth0.jwt.JWT.require;


@Slf4j
@Service
public class JWTService {
    @Value("${JWT.ISSUER}")
    private String ISSUER;

    @Value("${JWT.SECRET}")
    private String SECRET;

    /**
     * 토큰 생성
     *
     * @param user_idx 토큰에 담길 로그인한 사용자의 회원 고유 IDX
     * @return 토큰
     */
    public String create(final Long user_idx) {
        try {
            //토큰 생성 빌더 객체 생성
            JWTCreator.Builder b = JWT.create();
            //토큰 생성자 명시
            b.withIssuer(ISSUER);
            //토큰 payload 작성, key - value 형식, 객체도 가능
            b.withClaim("user_idx", user_idx);
            //만료
            b.withExpiresAt(expiresAt());
            //토큰 해싱해서 반환
            return b.sign(Algorithm.HMAC256(SECRET));
        } catch (JWTCreationException JwtCreationException) {
            log.info(JwtCreationException.getMessage());
        }
        return null;
    }

    private Date expiresAt() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        cal.add(Calendar.HOUR, 744);
        return cal.getTime();
    }

    /**
     * 토큰 해독
     *
     * @param token 토큰
     * @return 로그인한 사용자의 회원 고유 IDX
     */
    public JwtTokenVO decode(final String token) {
        try {
            //토큰 해독 객체 생성
            final JWTVerifier jwtVerifier = require(Algorithm.HMAC256(SECRET)).withIssuer(ISSUER).build();
            //토큰 검증
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            //토큰 payload 반환, 정상적인 토큰이라면 토큰 주인(사용자) 고유 ID, 아니라면 -1
            return new JwtTokenVO(decodedJWT.getClaim("user_idx").asLong());
        } catch (JWTVerificationException jve) {
            log.error(jve.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new JwtTokenVO();
    }

}
