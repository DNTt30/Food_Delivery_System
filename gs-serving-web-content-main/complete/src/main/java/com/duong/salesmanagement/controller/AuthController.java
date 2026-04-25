package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.service.AuthService;
import com.duong.salesmanagement.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    // =========================================================
    //  POST /api/auth/login
    // =========================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), authRequest.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername(), role));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Tài khoản chưa xác minh email! Vui lòng kiểm tra hộp thư."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // =========================================================
    //  POST /api/auth/register
    //  Body: { "username":"...", "password":"...", "fullName":"...",
    //           "email":"...", "role":"CUSTOMER" }
    // =========================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = authService.register(
                    req.getUsername(),
                    req.getPassword(),
                    req.getFullName(),
                    req.getEmail(),
                    req.getRole() != null ? req.getRole() : "CUSTOMER"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Đăng ký thành công! Mã xác minh đã được gửi đến " + user.getEmail(),
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Đăng ký thất bại: " + e.getMessage()));
        }
    }

    // =========================================================
    //  POST /api/auth/verify
    //  Body: { "email":"...", "code":"123456" }
    // =========================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req) {
        try {
            authService.verifyCode(req.getEmail(), req.getCode());
            return ResponseEntity.ok(Map.of(
                    "message", "Xác minh thành công! Bạn có thể đăng nhập ngay bây giờ."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    //  POST /api/auth/resend-code
    //  Body: { "email":"..." }
    // =========================================================
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            authService.resendCode(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Mã xác minh mới đã được gửi đến " + email
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    //  POST /api/auth/forgot-password
    //  Body: { "email":"..." }
    // =========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            authService.forgotPassword(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Mã xác nhận đặt lại mật khẩu đã được gửi đến " + email
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    //  POST /api/auth/reset-password
    //  Body: { "email":"...", "code":"...", "newPassword":"..." }
    // =========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            authService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới."
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
