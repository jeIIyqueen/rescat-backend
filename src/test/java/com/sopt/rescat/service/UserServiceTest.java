package com.sopt.rescat.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Test
    public void encodePassword() {
        System.out.println("암호화된 비밀번호 : " + userService.encodePassword("rescat"));
    }

    @Test
    public void decodePassword() {
        System.out.println("맞나요? : " + userService.decodePassword("$2a$10$JWxjd3ICDRUwK4XEpymtku6HU5cr7o2Z3iEt2ifDRLQUWJdKTLp8W"));
    }
}