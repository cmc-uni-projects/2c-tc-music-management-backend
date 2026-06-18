package com.example.CMCmp3.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateAlbumDTO {
    private String title;
    private String description;
    private MultipartFile imageFile;
    private String artistIds; // Comma-separated artist IDs
}
