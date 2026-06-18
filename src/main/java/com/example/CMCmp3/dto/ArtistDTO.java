package com.example.CMCmp3.dto;

import lombok.Data;

@Data
public class ArtistDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private boolean isVerified;
}
