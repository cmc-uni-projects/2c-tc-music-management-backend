package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreatePlaylistDTO;
import com.example.CMCmp3.dto.PlaylistDTO;
import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.dto.UpdatePlaylistDTO; // Import UpdatePlaylistDTO
import com.example.CMCmp3.dto.UpdatePlaylistSongsDTO;
import com.example.CMCmp3.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService; // Chỉ cần inject Service chính

    // Lấy tất cả
    @GetMapping
    public ResponseEntity<List<PlaylistDTO>> getAll() {
        return ResponseEntity.ok(playlistService.getAll());
    }

    // Lấy chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getById(id));
    }

    // API Top Nghe nhiều (Thay thế cho QueryService cũ)
    @GetMapping("/top")
    public ResponseEntity<List<PlaylistDTO>> getTop(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(playlistService.getTopPlaylistsByPlayCount(limit));
    }

    // API Top Mới nhất
    @GetMapping("/top/new-releases")
    public ResponseEntity<List<PlaylistDTO>> getTopNew(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(playlistService.getTopNewPlaylists(limit));
    }

    // API Top Lượt thích
    @GetMapping("/top/most-liked")
    public ResponseEntity<List<PlaylistDTO>> getTopLikes(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(playlistService.getTopPlaylistsByLikeCount(limit));
    }


//... (other imports)

    // Tạo mới
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaylistDTO> create(@Valid @ModelAttribute CreatePlaylistDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.createPlaylist(dto));
    }

    // Cập nhật thông tin playlist
    @PutMapping(value = "/{playlistId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaylistDTO> updatePlaylist(
            @PathVariable Long playlistId,
            @Valid @ModelAttribute UpdatePlaylistDTO dto) {
        return ResponseEntity.ok(playlistService.updatePlaylist(playlistId, dto));
    }

    // Xóa
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLikePlaylist(@PathVariable Long id) {
        playlistService.toggleLikePlaylist(id);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách Playlist của người dùng hiện tại
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlaylistDTO>> getMyPlaylists() {
        return ResponseEntity.ok(playlistService.findMyPlaylists());
    }

    // Lấy danh sách Playlist đã thích của người dùng hiện tại
    @GetMapping("/me/liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlaylistDTO>> getLikedPlaylistsForCurrentUser() {
        return ResponseEntity.ok(playlistService.getLikedPlaylistsForCurrentUser());
    }

    // Lấy danh sách bài hát trong một playlist cụ thể
    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<SongDTO>> getPlaylistSongs(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getSongsByPlaylistId(playlistId));
    }

    // Thêm/Xóa bài hát khỏi Playlist
    @PatchMapping("/{playlistId}/songs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SongDTO>> updatePlaylistSongs(
            @PathVariable Long playlistId,
            @RequestBody UpdatePlaylistSongsDTO dto) {
        return ResponseEntity.ok(playlistService.updateSongsInPlaylist(playlistId, dto));
    }

    // TODO: Configure the base URL in a more flexible way
    @GetMapping("/{id}/share")
    public ResponseEntity<String> sharePlaylist(@PathVariable Long id) {
        String shareUrl = "http://localhost:3000/playlists/" + id;
        return ResponseEntity.ok(shareUrl);
    }
}
