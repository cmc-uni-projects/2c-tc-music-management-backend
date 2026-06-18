package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

@Data
public class UpdatePlaylistDTO {
    private String name;

    private String description; // Add description field
    private MultipartFile imageFile; // To handle image upload
    private String artistIds; // Comma-separated artist IDs

    @Pattern(regexp = "(?i)PUBLIC|PRIVATE", message = "Chế độ riêng tư phải là 'PUBLIC' hoặc 'PRIVATE'")
    private String privacy;
}
