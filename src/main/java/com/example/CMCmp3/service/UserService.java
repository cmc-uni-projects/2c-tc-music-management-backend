package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.ChangePasswordDTO;
import com.example.CMCmp3.dto.RegisterDTO;
import com.example.CMCmp3.dto.UpdateUserDTO;
import com.example.CMCmp3.dto.UserDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IFileUploadService fileUploadService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Qualifier(value = "local-directory-upload-service") IFileUploadService fileUploadService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
    }

    // ======================================================
    // ✅ AUTO BACKFILL: ARTIST + NONE => APPROVED (dữ liệu cũ)
    // ======================================================
    @PostConstruct
    public void backfillLegacyArtistStatus() {
        try {
            int updated = userRepository.backfillArtistStatusForLegacyUsers();
            if (updated > 0) {
                log.info("✅ Backfill artistVerificationStatus: updated {} legacy ARTIST users", updated);
            } else {
                log.info("Backfill artistVerificationStatus: nothing to update");
            }
        } catch (Exception e) {
            // không chặn app chạy nếu backfill lỗi
            log.warn("Backfill artistVerificationStatus failed: {}", e.getMessage());
        }
    }

    public UserDTO convertToDTO(User u) {
        UserDTO dto = new UserDTO();

        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setDisplayName(u.getDisplayName());
        dto.setGender(u.getGender());
        dto.setPhoneNumber(u.getPhone());
        dto.setAvatarUrl(u.getAvatarUrl());

        // roles dạng Set<String> cho FE
        dto.setRoles(Set.of("ROLE_" + u.getRole().name()));

        // role đơn (ADMIN/ARTIST/USER)
        dto.setRole(u.getRole() != null ? u.getRole().name() : null);

        // ✅ không còn hack nữa: status trả đúng từ DB
        dto.setArtistVerificationStatus(
                u.getArtistVerificationStatus() != null
                        ? u.getArtistVerificationStatus().name()
                        : ArtistVerificationStatus.NONE.name()
        );

        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        dto.setLastLoginTime(u.getLastLoginTime());
        dto.setProvider(u.getProvider().name());
        dto.setTwoFactorEnabled(u.isTwoFactorEnabled());

        return dto;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserFromAuthentication(Authentication authentication) {
        return getCurrentUser(authentication);
    }

    public User registerUser(RegisterDTO registerDTO) {
        final String email = safeLower(registerDTO.getEmail());
        final String displayName = safeTrim(registerDTO.getDisplayName());
        final String phone = safeTrim(registerDTO.getPhone());
        final String avatarUrl = safeTrim(registerDTO.getAvatarUrl());

        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("Email không được để trống");
        }

        var existingOpt = userRepository.findByEmailIgnoreCase(email);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();

            if (existing.getStatus() == UserStatus.ACTIVE) {
                throw new RuntimeException("Email đã được đăng ký");
            }
            if (StringUtils.hasText(phone)
                    && !phone.equals(existing.getPhone())
                    && userRepository.existsByPhone(phone)) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }

            updateUserFromDTO(existing, displayName, email, phone, avatarUrl, registerDTO.getDob(), registerDTO.getGender());
            existing.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            existing.setStatus(UserStatus.ACTIVE);
            existing.setRole(Role.USER);
            existing.setProvider(AuthProvider.LOCAL);

            // ✅ đảm bảo default status
            if (existing.getArtistVerificationStatus() == null) {
                existing.setArtistVerificationStatus(ArtistVerificationStatus.NONE);
            }

            return userRepository.save(existing);
        }

        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        String username = generateUniqueUsername(displayName);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .artistVerificationStatus(ArtistVerificationStatus.NONE) // ✅ default
                .build();

        updateUserFromDTO(user, displayName, email, phone, avatarUrl, registerDTO.getDob(), registerDTO.getGender());
        return userRepository.save(user);
    }

    public User authenticate(String emailRaw, String rawPassword) {
        String email = safeLower(emailRaw);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(safeLower(email));
    }

    public UserDTO getMe(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO toggleTwoFactorAuthentication(Authentication authentication) {
        User user = getCurrentUser(authentication);

        boolean initial2FAStatus = user.isTwoFactorEnabled();
        log.info("User {} (ID: {}) initial 2FA status: {}", user.getEmail(), user.getId(), initial2FAStatus);

        user.setTwoFactorEnabled(!initial2FAStatus);
        User updatedUser = userRepository.save(user);

        log.info("User {} (ID: {}) 2FA status after save: {}", updatedUser.getEmail(), updatedUser.getId(), updatedUser.isTwoFactorEnabled());
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO updateMe(Authentication authentication, UpdateUserDTO userDTO) {
        User user = getCurrentUser(authentication);
        user.setDisplayName(userDTO.getDisplayName());
        Gender gender = Gender.fromString(userDTO.getGender());
        user.setGender(gender);
        user.setPhone(userDTO.getPhoneNumber());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO updateAvatar(Authentication authentication, MultipartFile file) {
        User user = getCurrentUser(authentication);

        try {
            String fileDownloadUri = fileUploadService.uploadFile(file);
            user.setAvatarUrl(fileDownloadUri);
            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file. Vui lòng thử lại!", ex);
        }
    }

    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordDTO dto) {
        User user = getCurrentUser(authentication);

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new RuntimeException("Không thể đổi mật khẩu cho tài khoản đã liên kết với " + user.getProvider() + ".");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public void updatePhone(Long userId, String newPhone) {
        String phone = safeTrim(newPhone);
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        u.setPhone(phone);
        userRepository.save(u);
    }

    private void updateUserFromDTO(
            User user, String displayName, String email, String phone,
            String avatarUrl, String dob, String gender) {

        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAvatarUrl(avatarUrl);

        if (StringUtils.hasText(dob)) {
            try {
                user.setDob(LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception ignored) {}
        }
        if (StringUtils.hasText(gender)) {
            try {
                user.setGender(Gender.valueOf(gender.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private String generateUniqueUsername(String base) {
        String cleanBase = safeTrim(base).replaceAll("[^\\p{L}0-9]", "").toLowerCase();
        cleanBase = StringUtils.hasText(cleanBase) ? cleanBase : "user";
        String username;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 6);
            username = cleanBase + suffix;
        } while (userRepository.existsByUsername(username));
        return username;
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static String safeLower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
}
