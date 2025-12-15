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
@Slf4j // Dùng để log (in ra console)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "admin123";

        // 1. Kiểm tra xem admin đã tồn tại chưa
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Tài khoản admin ('{}') không tồn tại. Đang tiến hành tạo...", adminEmail);

            // 2. Nếu chưa, tạo tài khoản mới
            User adminUser = User.builder()
                    .email(adminEmail)
                    .username(adminEmail) // Dùng email làm username
                    .displayName("Admin")
                    .password(passwordEncoder.encode(adminPassword)) // Mã hóa mật khẩu
                    .role(Role.ADMIN) // Gán quyền ADMIN
                    .status(UserStatus.ACTIVE) // Trạng thái kích hoạt
                    .provider(AuthProvider.LOCAL) // Đăng nhập local (không phải Google)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // 3. Lưu vào CSDL
            userRepository.save(adminUser);

            log.info("Tạo tài khoản admin thành công!");
        } else {
            log.info("Tài khoản admin ('{}') đã tồn tại. Bỏ qua.", adminEmail);
        }
    }
}