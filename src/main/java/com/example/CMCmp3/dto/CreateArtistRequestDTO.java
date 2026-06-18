package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArtistRequestDTO {
    @NotBlank(message = "Tên nghệ sĩ không được để trống")
    private String artistName;

    @NotBlank(message = "Ảnh đại diện không được để trống")
    private String imageUrl;
}
