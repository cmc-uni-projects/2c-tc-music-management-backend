package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.Flag; // Import for Pattern.Flag
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePlaylistDTO {
    @NotBlank(message = "Tên playlist không được để trống")
    private String name; // Changed from title to name

    private String description;
    private MultipartFile imageFile; // To handle image upload

    @Pattern(regexp = "PUBLIC|PRIVATE", flags = Flag.CASE_INSENSITIVE, message = "Chế độ riêng tư phải là 'PUBLIC' hoặc 'PRIVATE'")
    private String privacy; // 'public' hoặc 'private'
}