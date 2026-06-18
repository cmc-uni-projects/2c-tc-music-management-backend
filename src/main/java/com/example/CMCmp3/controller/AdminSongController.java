package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/songs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSongController {

    private final SongService songService;

    @GetMapping("/pending")
    public ResponseEntity<Page<SongDTO>> getPendingSongs(Pageable pageable) {
        Page<SongDTO> pendingSongs = songService.getSongsByStatus(com.example.CMCmp3.entity.SongStatus.PENDING, pageable);
        return ResponseEntity.ok(pendingSongs);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SongDTO> approveSong(@PathVariable Long id) {
        SongDTO approvedSong = songService.changeSongStatus(id, com.example.CMCmp3.entity.SongStatus.APPROVED);
        return ResponseEntity.ok(approvedSong);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<SongDTO> rejectSong(@PathVariable Long id) {
        SongDTO rejectedSong = songService.changeSongStatus(id, com.example.CMCmp3.entity.SongStatus.REJECTED);
        return ResponseEntity.ok(rejectedSong);
    }
}
