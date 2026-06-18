package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.ArtistRequestDTO;
import com.example.CMCmp3.dto.CreateArtistRequestDTO;
import com.example.CMCmp3.entity.ArtistVerificationRequest;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.service.ArtistVerificationService;
import com.example.CMCmp3.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/artist-verification-requests")
@RequiredArgsConstructor
public class ArtistVerificationController {

    private final ArtistVerificationService artistVerificationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createArtistVerificationRequest(
            Authentication authentication,
            @Valid @RequestBody CreateArtistRequestDTO requestDTO) {
        try {
            User user = userService.getUserFromAuthentication(authentication);
            ArtistVerificationRequest request = artistVerificationService.createRequest(requestDTO, user);
            return ResponseEntity.ok(ArtistRequestDTO.fromEntity(request));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
