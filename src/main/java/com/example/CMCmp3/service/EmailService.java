package com.example.CMCmp3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Thêm cái này để ghi log
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            log.info("Bắt đầu gửi OTP đến email: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Mã xác thực OTP - CMCmp3");
            message.setText("Mã OTP của bạn là: " + otp);

            mailSender.send(message);

            log.info("Đã gửi mail thành công cho: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi mail (Async): {}", e.getMessage());
        }
    }
}