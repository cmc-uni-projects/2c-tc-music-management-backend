package com.example.CMCmp3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private List<SongDTO> songs;
    private List<ArtistDTO> artists;
    private List<PlaylistDTO> playlists;
    private List<TagDTO> tags;
}