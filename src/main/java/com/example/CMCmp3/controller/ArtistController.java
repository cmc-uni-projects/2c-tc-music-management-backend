package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.ArtistDTO;

import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.dto.UpdateArtistDTO;
import com.example.CMCmp3.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<List<SongDTO>> getSongsByArtistId(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getSongsByArtistId(id));
    }

    @GetMapping("/by-song")
    public ResponseEntity<List<ArtistDTO>> getArtistsBySongTitle(@RequestParam String songTitle) {
        return ResponseEntity.ok(artistService.findArtistsBySongTitle(songTitle));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistDTO> createArtist(
            @RequestParam("name") String name,
            @RequestParam("imageFile") MultipartFile imageFile) {
        ArtistDTO newArtist = artistService.createArtistWithUpload(name, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArtist);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtistDTO> updateArtist(@PathVariable Long id, @RequestBody UpdateArtistDTO updateArtistDTO) {
        ArtistDTO updatedArtist = artistService.updateArtist(id, updateArtistDTO);
        return ResponseEntity.ok(updatedArtist);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}