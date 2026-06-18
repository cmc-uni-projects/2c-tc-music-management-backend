package com.example.CMCmp3.dto;

import com.example.CMCmp3.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String email;
    private String displayName;
    private Gender gender;
    private String phoneNumber;
    private String avatarUrl;

    private Set<String> roles;

    // ✅ HƯỚNG A
    private String role; // ADMIN/ARTIST/USER
    private String artistVerificationStatus; // NONE/PENDING/APPROVED/REJECTED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginTime;
    private String provider;
    private boolean isTwoFactorEnabled;
}
