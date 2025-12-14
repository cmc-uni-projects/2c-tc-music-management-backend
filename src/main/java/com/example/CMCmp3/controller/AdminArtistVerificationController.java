package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.ArtistRequestDTO;
import com.example.CMCmp3.entity.ArtistVerificationRequest;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.service.ArtistVerificationService;
import com.example.CMCmp3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/artist-verification-requests")
@RequiredArgsConstructor
public class AdminArtistVerificationController {

    private final ArtistVerificationService artistVerificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ArtistRequestDTO>> getPendingRequests() {
        List<ArtistVerificationRequest> requests = artistVerificationService.getPendingRequests();
        List<ArtistRequestDTO> dtos = requests.stream()
                .map(ArtistRequestDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = userService.getUserFromAuthentication(authentication);
            artistVerificationService.approveRequest(id, admin);
            return ResponseEntity.ok("Request approved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = userService.getUserFromAuthentication(authentication);
            artistVerificationService.rejectRequest(id, admin);
            return ResponseEntity.ok("Request rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
