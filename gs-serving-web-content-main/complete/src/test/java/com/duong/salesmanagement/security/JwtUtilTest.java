package com.duong.salesmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    public void whenGenerateToken_thenExtractUsername() {
        // when
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);

        // then
        assertEquals("testuser", username);
    }

    @Test
    public void whenValidateToken_thenReturnTrue() {
        // given
        String token = jwtUtil.generateToken(userDetails);

        // when
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // then
        assertTrue(isValid);
    }
}
