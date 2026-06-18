package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.UpdateUserDTO;
import com.example.CMCmp3.dto.UserDTO;
import com.example.CMCmp3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDTO> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.getMe(authentication));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateMe(Authentication authentication, @RequestBody UpdateUserDTO userDTO) {
        return ResponseEntity.ok(userService.updateMe(authentication, userDTO));
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserDTO> updateAvatar(Authentication authentication, @RequestParam("avatar") MultipartFile file) {
        return ResponseEntity.ok(userService.updateAvatar(authentication, file));
    }

    @PostMapping("/toggle-2fa")
    public ResponseEntity<UserDTO> toggleTwoFactor(Authentication authentication) {
        return ResponseEntity.ok(userService.toggleTwoFactorAuthentication(authentication));
    }
}
