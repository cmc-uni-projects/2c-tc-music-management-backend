package com.example.CMCmp3.config;

import com.example.CMCmp3.entity.AuthProvider;
import com.example.CMCmp3.entity.Role;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.entity.UserStatus;
import com.example.CMCmp3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "admin123";

        // chek tk admin có chưua
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Tài khoản admin ('{}') không tồn tại. Đang tiến hành tạo...", adminEmail);

            // tạo mới nếu chưa có
            User adminUser = User.builder()
                    .email(adminEmail)
                    .username(adminEmail)
                    .displayName("Admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN) // Gán quyền ADMIN
                    .status(UserStatus.ACTIVE)
                    .provider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // lưu vào CSDL
            userRepository.save(adminUser);

            log.info("Tạo tài khoản admin thành công!");
        } else {
            log.info("Tài khoản admin ('{}') đã tồn tại. Bỏ qua.", adminEmail);
        }
    }
}