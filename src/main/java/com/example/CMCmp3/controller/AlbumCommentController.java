package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreateAlbumCommentDTO;
import com.example.CMCmp3.dto.AlbumCommentDTO;
import com.example.CMCmp3.dto.UpdateAlbumCommentDTO;
import com.example.CMCmp3.service.AlbumCommentService;
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
@RequestMapping("/api/albums/{albumId}/comments")
@RequiredArgsConstructor
public class AlbumCommentController {

    private final AlbumCommentService albumCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlbumCommentDTO> addComment(
            @PathVariable Long albumId,
            @Valid @RequestBody CreateAlbumCommentDTO commentDTO) {
        AlbumCommentDTO newComment = albumCommentService.addCommentToAlbum(albumId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @GetMapping
    public ResponseEntity<Page<AlbumCommentDTO>> getComments(
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AlbumCommentDTO> comments = albumCommentService.getCommentsByAlbumId(albumId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlbumCommentDTO> updateComment(
            @PathVariable Long albumId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateAlbumCommentDTO commentDTO) {
        AlbumCommentDTO updatedComment = albumCommentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long albumId,
            @PathVariable Long commentId) {
        albumCommentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AlbumCommentDTO>> getPendingComments(
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AlbumCommentDTO> comments = albumCommentService.getPendingCommentsByAlbumId(albumId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{commentId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> approveComment(
            @PathVariable Long albumId,
            @PathVariable Long commentId) {
        albumCommentService.approveComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> rejectComment(
            @PathVariable Long albumId,
            @PathVariable Long commentId) {
        albumCommentService.rejectComment(commentId);
        return ResponseEntity.ok().build();
    }
}
