package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.StatsSummaryDTO;
import com.example.CMCmp3.repository.ArtistRepository;
import com.example.CMCmp3.repository.PlaylistRepository;
import com.example.CMCmp3.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional(readOnly = true)
    public StatsSummaryDTO getStatsSummary() {
        long totalSongs = songRepository.count();
        long totalArtists = artistRepository.count();
        long totalPlaylists = playlistRepository.count();
        return new StatsSummaryDTO(totalSongs, totalArtists, totalPlaylists);
    }
}
