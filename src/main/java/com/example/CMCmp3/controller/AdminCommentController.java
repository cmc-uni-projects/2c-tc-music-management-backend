package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.PlaylistCommentDTO;
import com.example.CMCmp3.dto.SongCommentDTO;
import com.example.CMCmp3.service.PlaylistCommentService;
import com.example.CMCmp3.service.SongCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private final SongCommentService songCommentService;
    private final PlaylistCommentService playlistCommentService;

    @GetMapping("/songs/pending")
    public ResponseEntity<Page<SongCommentDTO>> getPendingSongComments(Pageable pageable) {
        return ResponseEntity.ok(songCommentService.getPendingComments(pageable));
    }

    @PostMapping("/songs/{commentId}/approve")
    public ResponseEntity<Void> approveSongComment(@PathVariable Long commentId) {
        songCommentService.approveComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/songs/{commentId}/reject")
    public ResponseEntity<Void> rejectSongComment(@PathVariable Long commentId) {
        songCommentService.rejectComment(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/playlists/pending")
    public ResponseEntity<Page<PlaylistCommentDTO>> getPendingPlaylistComments(Pageable pageable) {
        return ResponseEntity.ok(playlistCommentService.getPendingComments(pageable));
    }

    @PostMapping("/playlists/{commentId}/approve")
    public ResponseEntity<Void> approvePlaylistComment(@PathVariable Long commentId) {
        playlistCommentService.approveComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/playlists/{commentId}/reject")
    public ResponseEntity<Void> rejectPlaylistComment(@PathVariable Long commentId) {
        playlistCommentService.rejectComment(commentId);
        return ResponseEntity.ok().build();
    }
}
