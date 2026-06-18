package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreateSongCommentDTO;
import com.example.CMCmp3.dto.SongCommentDTO;
import com.example.CMCmp3.dto.UpdateSongCommentDTO;
import com.example.CMCmp3.service.SongCommentService;
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
@RequestMapping("/api/songs/{songId}/comments")
@RequiredArgsConstructor
public class SongCommentController {

    private final SongCommentService songCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SongCommentDTO> addComment(
            @PathVariable Long songId,
            @Valid @RequestBody CreateSongCommentDTO commentDTO) {
        SongCommentDTO newComment = songCommentService.addCommentToSong(songId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @GetMapping
    public ResponseEntity<Page<SongCommentDTO>> getComments(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SongCommentDTO> comments = songCommentService.getCommentsBySongId(songId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SongCommentDTO> updateComment(
            @PathVariable Long songId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateSongCommentDTO commentDTO) {
        SongCommentDTO updatedComment = songCommentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long songId,
            @PathVariable Long commentId) {
        songCommentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SongCommentDTO>> getPendingComments(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SongCommentDTO> comments = songCommentService.getPendingCommentsBySongId(songId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{commentId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> approveComment(
            @PathVariable Long songId,
            @PathVariable Long commentId) {
        songCommentService.approveComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> rejectComment(
            @PathVariable Long songId,
            @PathVariable Long commentId) {
        songCommentService.rejectComment(commentId);
        return ResponseEntity.ok().build();
    }
}
