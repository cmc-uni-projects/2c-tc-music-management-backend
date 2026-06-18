package com.example.CMCmp3.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateSongDTO {

    private String title;

    private String filePath;

    private String imageUrl;

    private String description;

    private Set<Long> artistIds;

    private Set<Long> tagIds;
}

