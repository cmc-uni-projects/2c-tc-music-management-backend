package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreateSongDTO;
import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.dto.AddLyricsDTO;
import com.example.CMCmp3.dto.SongSearchResponseDTO;
import com.example.CMCmp3.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(songService.getAllSongs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getById(id));
    }

    @GetMapping("/details")
    public ResponseEntity<SongDTO> getSongByTitle(@RequestParam String title) {
        return ResponseEntity.ok(songService.getSongByTitle(title));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<SongDTO>> getSimilarSongs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(songService.getSimilarSongs(id, limit));
    }

    @GetMapping("/similar-by-title")
    public ResponseEntity<List<SongDTO>> getSimilarSongsByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(songService.getSimilarSongsByTitle(title, limit));
    }

    /* ================== DOWNLOAD (cần đăng nhập) ================== */

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadSong(@PathVariable Long id) throws IOException {
        Map<String, Object> songData = songService.getSongResource(id);
        Resource resource = (Resource) songData.get("resource");
        String filename = (String) songData.get("filename");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /* ================== STREAM (PUBLIC – dùng cho ZingChart, player, v.v.) ================== */

    @GetMapping("/stream/{id}")
    @PreAuthorize("permitAll()")   // hoặc bỏ hẳn @PreAuthorize nếu class không bị chặn chung
    public ResponseEntity<Resource> streamSong(@PathVariable Long id) throws IOException {
        Map<String, Object> songData = songService.getSongResource(id);
        Resource resource = (Resource) songData.get("resource");
        String filename   = (String) songData.get("filename");
        String contentType = (String) songData.getOrDefault("contentType", "audio/mpeg");

        return ResponseEntity.ok()
                // cho phép trình duyệt/audio tag phát trực tiếp
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    /* ================== TOP SONGS ================== */

    @GetMapping("/top")
    public ResponseEntity<List<SongDTO>> getTop(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(songService.getTopSongs(limit));
    }

    @GetMapping("/top/new-releases")
    public ResponseEntity<List<SongDTO>> getTopNew(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(songService.getTopNewReleases(limit));
    }

    @GetMapping("/top/most-liked")
    public ResponseEntity<List<SongDTO>> getTopLiked(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(songService.getTopMostLiked(limit));
    }

    /* ================== UPLOADED / FAVORITES ================== */

    @GetMapping("/uploaded")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SongDTO>> getUploadedSongs(@RequestParam(name = "q", required = false) String query) {
        return ResponseEntity.ok(songService.getUploadedSongsForCurrentUser(query));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SongDTO>> getSongsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(songService.getSongsByUserId(userId));
    }

    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SongDTO>> getFavoriteSongs() {
        return ResponseEntity.ok(songService.getFavoriteSongsForCurrentUser());
    }

    @GetMapping("/{id}/is-uploader")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isUploader(@PathVariable Long id) {
        return ResponseEntity.ok(songService.isUploader(id));
    }

    @GetMapping("/by-artist")
    public ResponseEntity<List<SongDTO>> getSongsByArtistName(@RequestParam String artistName) {
        return ResponseEntity.ok(songService.findSongsByArtistName(artistName));
    }

    @GetMapping("/by-tag")
    public ResponseEntity<List<SongDTO>> getSongsByTagName(
            @RequestParam String tagName,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(songService.findSongsByMood(tagName, limit));
    }

    /* ================== CRUD ================== */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SongDTO> create(@Valid @RequestBody CreateSongDTO dto) {
        return ResponseEntity.ok(songService.createSong(dto));
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SongDTO> uploadSong(
            @RequestParam("songFile") MultipartFile songFile,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "artistIds", required = false) Set<Long> artistIds,
            @RequestParam(value = "tagIds", required = false) Set<Long> tagIds) {

        SongDTO newSong = songService.createSongWithUpload(
                title, description, artistIds, tagIds, songFile, imageFile
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(newSong);
    }

    @PostMapping("/{id}/lyrics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SongDTO> addLyrics(@PathVariable Long id, @RequestBody AddLyricsDTO dto) {
        return ResponseEntity.ok(songService.addLyricsToSong(id, dto));
    }

    @PutMapping(value = "/uploaded/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SongDTO> updateUploadedSong(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "artistIds", required = false) Set<Long> artistIds,
            @RequestParam(value = "tagIds", required = false) Set<Long> tagIds,
            @RequestParam(value = "songFile", required = false) MultipartFile songFile,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "status", required = false) String status
    ) {
        SongDTO updatedSong = songService.updateUploadedSong(
                id, title, description, artistIds, tagIds, songFile, imageFile, status
        );
        return ResponseEntity.ok(updatedSong);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SongDTO> update(@PathVariable Long id,
                                          @RequestBody CreateSongDTO dto) {
        return ResponseEntity.ok(songService.updateSong(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    /* ================== LIKE / UNLIKE ================== */

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        songService.likeSong(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlike(@PathVariable Long id) {
        songService.unlikeSong(id);
        return ResponseEntity.noContent().build();
    }

    /* ================== LISTEN COUNT ================== */

    @PutMapping("/{id}/listen")
    public ResponseEntity<Void> incrementListenCount(@PathVariable Long id) {
        songService.incrementListenCount(id);
        return ResponseEntity.ok().build();
    }

    /* ================== SHARE LINK ================== */

    @GetMapping("/{id}/share")
    public ResponseEntity<String> shareSong(@PathVariable Long id) {
        String shareUrl = "http://localhost:3000/songs/" + id;
        return ResponseEntity.ok(shareUrl);
    }

    @GetMapping("/search/lyric")
    public ResponseEntity<List<SongSearchResponseDTO>> searchByLyric(@RequestParam("query") String query) {
        return ResponseEntity.ok(songService.searchByLyric(query));
    }
}
