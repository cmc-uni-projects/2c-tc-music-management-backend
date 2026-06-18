package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreatePlaylistCommentDTO;
import com.example.CMCmp3.dto.PlaylistCommentDTO;
import com.example.CMCmp3.dto.UpdatePlaylistCommentDTO;
import com.example.CMCmp3.service.PlaylistCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists/{playlistId}/comments")
@RequiredArgsConstructor
public class PlaylistCommentController {

    private final PlaylistCommentService playlistCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaylistCommentDTO> addComment(
            @PathVariable Long playlistId,
            @Valid @RequestBody CreatePlaylistCommentDTO commentDTO) {
        PlaylistCommentDTO newComment = playlistCommentService.addCommentToPlaylist(playlistId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @GetMapping
    public ResponseEntity<Page<PlaylistCommentDTO>> getComments(
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PlaylistCommentDTO> comments = playlistCommentService.getCommentsByPlaylistId(playlistId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaylistCommentDTO> updateComment(
            @PathVariable Long playlistId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdatePlaylistCommentDTO commentDTO) {
        PlaylistCommentDTO updatedComment = playlistCommentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long playlistId,
            @PathVariable Long commentId) {
        playlistCommentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlaylistCommentDTO>> getPendingComments(
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PlaylistCommentDTO> comments = playlistCommentService.getPendingCommentsByPlaylistId(playlistId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{commentId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> approveComment(
            @PathVariable Long playlistId,
            @PathVariable Long commentId) {
        playlistCommentService.approveComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> rejectComment(
            @PathVariable Long playlistId,
            @PathVariable Long commentId) {
        playlistCommentService.rejectComment(commentId);
        return ResponseEntity.ok().build();
    }
}
