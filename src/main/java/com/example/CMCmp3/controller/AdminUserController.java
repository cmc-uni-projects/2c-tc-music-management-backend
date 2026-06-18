package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.UserDTO;
import com.example.CMCmp3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> list(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // Gọi hàm getAllUsers
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }


    @PutMapping("/{id}/phone")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePhone(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        userService.updatePhone(id, body.getOrDefault("phone", ""));
        return ResponseEntity.noContent().build();
    }
}