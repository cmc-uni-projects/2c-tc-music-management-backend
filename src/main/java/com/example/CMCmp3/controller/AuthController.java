package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.*;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.security.JwtService;
import com.example.CMCmp3.service.OtpService;
import com.example.CMCmp3.service.OtpVerificationResult;
import com.example.CMCmp3.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequestDTO request) {
        System.out.println("Received OTP request for email: " + request.getEmail());
        try {
            otpService.generateAndSendOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO request) {
        try {
            OtpVerificationResult otpResult = otpService.verifyOtp(request.getEmail(), request.getOtp());
            switch (otpResult) {
                case INVALID:
                    return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP không hợp lệ"));
                case EXPIRED:
                    return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP đã hết hạn"));
                case SUCCESS:
                    // Proceed with registration
                    break;
            }

            User user = userService.registerUser(request);
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Đăng ký thành công",
                    "username", user.getUsername(),
                    "token", token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {
        try {
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            if (user.isTwoFactorEnabled()) {
                // 2FA is enabled, proceed with OTP flow
                otpService.generateAndSendOtpForLogin(user.getEmail());
                return ResponseEntity.ok(Map.of(
                        "message", "Xác thực thành công, vui lòng nhập mã OTP"
                ));
            } else {
                // 2FA is not enabled, log in directly
                String token = jwtService.generateToken(user);
                UserDTO userDTO = userService.convertToDTO(user);
                return ResponseEntity.ok(Map.of(
                        "message", "Đăng nhập thành công",
                        "user", userDTO,
                        "token", token
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody VerifyLoginOtpDTO request) {
        try {
            OtpVerificationResult otpResult = otpService.verifyOtp(request.getEmail(), request.getOtp());
            switch (otpResult) {
                case INVALID:
                    return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP không hợp lệ"));
                case EXPIRED:
                    return ResponseEntity.badRequest().body(Map.of("error", "Mã OTP đã hết hạn"));
                case SUCCESS:
                    // Proceed with login
                    break;
            }

            User user = userService.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
            String token = jwtService.generateToken(user);
            UserDTO userDTO = userService.convertToDTO(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Đăng nhập thành công",
                    "user", userDTO,
                    "token", token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody OtpRequestDTO request) {
        try {
            otpService.generateAndSendOtpForPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody com.example.CMCmp3.dto.ResetPasswordDTO request) {
        try {
            OtpVerificationResult otpResult = otpService.verifyOtp(request.getEmail(), request.getOtp());
            switch (otpResult) {
                case INVALID:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP"));
                case EXPIRED:
                    return ResponseEntity.badRequest().body(Map.of("error", "Expired OTP"));
                case SUCCESS:
                    // Proceed with password reset
                    break;
            }

            userService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
