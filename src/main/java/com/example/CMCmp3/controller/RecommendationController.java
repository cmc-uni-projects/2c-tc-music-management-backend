package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.service.SongService;
import com.example.CMCmp3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final SongService songService;
    private final UserService userService;

    @GetMapping("/songs")
    public ResponseEntity<List<SongDTO>> getRecommendationsByMood(
            @RequestParam String mood,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(songService.findSongsByMood(mood, limit));
    }

    @GetMapping("/for-me")
    public ResponseEntity<List<SongDTO>> getRecommendationsForMe(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit
    ) {
        User user = userService.getUserFromAuthentication(authentication);
        return ResponseEntity.ok(songService.getRecommendationsForUser(user.getId(), limit));
    }
}
