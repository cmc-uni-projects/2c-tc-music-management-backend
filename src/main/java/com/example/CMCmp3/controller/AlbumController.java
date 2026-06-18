package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreateAlbumDTO;
import com.example.CMCmp3.dto.AlbumDTO;
import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.dto.UpdateAlbumDTO;
import com.example.CMCmp3.dto.UpdateAlbumSongsDTO;
import com.example.CMCmp3.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public ResponseEntity<List<AlbumDTO>> getAll() {
        return ResponseEntity.ok(albumService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getById(id));
    }

    @GetMapping("/top")
    public ResponseEntity<List<AlbumDTO>> getTop(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(albumService.getTopAlbumsByPlayCount(limit));
    }

    @GetMapping("/top/new-releases")
    public ResponseEntity<List<AlbumDTO>> getTopNew(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(albumService.getTopNewAlbums(limit));
    }

    @GetMapping("/top/most-liked")
    public ResponseEntity<List<AlbumDTO>> getTopLikes(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(albumService.getTopAlbumsByLikeCount(limit));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN', 'ARTIST')")
    public ResponseEntity<AlbumDTO> create(@Valid @ModelAttribute CreateAlbumDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumService.createAlbum(dto));
    }

    @PutMapping(value = "/{albumId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlbumDTO> updateAlbum(
            @PathVariable Long albumId,
            @Valid @ModelAttribute UpdateAlbumDTO dto) {
        return ResponseEntity.ok(albumService.updateAlbum(albumId, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLikeAlbum(@PathVariable Long id) {
        albumService.toggleLikeAlbum(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/me/liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AlbumDTO>> getLikedAlbumsForCurrentUser() {
        return ResponseEntity.ok(albumService.getLikedAlbumsForCurrentUser());
    }

    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<SongDTO>> getAlbumSongs(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.getSongsByAlbumId(albumId));
    }

    @PatchMapping("/{albumId}/songs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SongDTO>> updateAlbumSongs(
            @PathVariable Long albumId,
            @RequestBody UpdateAlbumSongsDTO dto) {
        return ResponseEntity.ok(albumService.updateSongsInAlbum(albumId, dto));
    }

    @GetMapping("/{id}/share")
    public ResponseEntity<String> shareAlbum(@PathVariable Long id) {
        String shareUrl = "http://localhost:3000/albums/" + id;
        return ResponseEntity.ok(shareUrl);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AlbumDTO>> getMyCreatedAlbums() {
        return ResponseEntity.ok(albumService.getAlbumsForCurrentUser());
    }
}
