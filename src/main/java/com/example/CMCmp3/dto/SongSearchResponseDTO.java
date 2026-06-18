package com.example.CMCmp3.dto;

import lombok.Data;

import java.util.Set;

@Data
public class SongSearchResponseDTO {
    private Long id;
    private String title;
    private Set<TagDTO> tags;
    private String artistName;
}
