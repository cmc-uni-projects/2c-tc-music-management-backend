package com.example.CMCmp3.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateAlbumSongsDTO {
    private List<Long> add; // List of song IDs to add
    private List<Long> remove; // List of song IDs to remove
}
