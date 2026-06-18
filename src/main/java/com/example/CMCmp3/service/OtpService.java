package com.example.CMCmp3.service;

import com.example.CMCmp3.entity.OtpVerification;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.entity.UserStatus;
import com.example.CMCmp3.repository.OtpVerificationRepository;
import com.example.CMCmp3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public void generateAndSendOtp(String email) {
        // Check if user with this email already exists and is active
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isPresent() && existingUser.get().getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản này đã được sử dụng");
        }

        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        Optional<OtpVerification> existingOtp = otpVerificationRepository.findByEmail(email);
        OtpVerification otpVerification;
        if (existingOtp.isPresent()) {
            otpVerification = existingOtp.get();
            otpVerification.setOtp(otp);
            otpVerification.setExpiryTime(expiryTime);
        } else {
            otpVerification = OtpVerification.builder()
                    .email(email)
                    .otp(otp)
                    .expiryTime(expiryTime)
                    .build();
        }
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otp);
    }

    public void generateAndSendOtpForPasswordReset(String email) {
        // Check if user with this email exists and is active
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isEmpty() || existingUser.get().getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản không tồn tại hoặc chưa được kích hoạt");
        }

        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        Optional<OtpVerification> existingOtp = otpVerificationRepository.findByEmail(email);
        OtpVerification otpVerification;
        if (existingOtp.isPresent()) {
            otpVerification = existingOtp.get();
            otpVerification.setOtp(otp);
            otpVerification.setExpiryTime(expiryTime);
        } else {
            otpVerification = OtpVerification.builder()
                    .email(email)
                    .otp(otp)
                    .expiryTime(expiryTime)
                    .build();
        }
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otp);
    }

    public void generateAndSendOtpForLogin(String email) {
        // Check if user with this email exists
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("Tài khoản không tồn tại");
        }

        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        Optional<OtpVerification> existingOtp = otpVerificationRepository.findByEmail(email);
        OtpVerification otpVerification;
        if (existingOtp.isPresent()) {
            otpVerification = existingOtp.get();
            otpVerification.setOtp(otp);
            otpVerification.setExpiryTime(expiryTime);
        } else {
            otpVerification = OtpVerification.builder()
                    .email(email)
                    .otp(otp)
                    .expiryTime(expiryTime)
                    .build();
        }
        otpVerificationRepository.save(otpVerification);
        emailService.sendOtpEmail(email, otp);
    }

    public OtpVerificationResult verifyOtp(String email, String otp) {
        Optional<OtpVerification> otpVerificationOptional = otpVerificationRepository.findByEmailAndOtp(email, otp);
        if (otpVerificationOptional.isEmpty()) {
            return OtpVerificationResult.INVALID;
        }

        OtpVerification otpVerification = otpVerificationOptional.get();
        if (otpVerification.getExpiryTime().isBefore(LocalDateTime.now())) {
            return OtpVerificationResult.EXPIRED;
        }

        // OTP is valid and not expired
        otpVerificationRepository.delete(otpVerification); // OTP can be used only once
        return OtpVerificationResult.SUCCESS;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
