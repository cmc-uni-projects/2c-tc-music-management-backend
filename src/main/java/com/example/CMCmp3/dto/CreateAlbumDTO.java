package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateAlbumDTO {
    @NotBlank(message = "Tên album không được để trống")
    private String title;

    private String description;
    private MultipartFile imageFile;
}
