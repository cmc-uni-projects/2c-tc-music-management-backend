package com.example.CMCmp3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsSummaryDTO {
    private long totalSongs;
    private long totalArtists;
    private long totalPlaylists;
}
