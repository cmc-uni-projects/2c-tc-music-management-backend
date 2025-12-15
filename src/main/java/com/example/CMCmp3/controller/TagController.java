package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.TagDTO;
import com.example.CMCmp3.dto.CreateTagDTO;
import com.example.CMCmp3.dto.UpdateTagDTO;
import com.example.CMCmp3.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Page<TagDTO>> getAllTags(@PageableDefault(size = 20) Pageable pageable) {
        Page<TagDTO> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody CreateTagDTO createTagDTO) {
        TagDTO newTag = tagService.createTag(createTagDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Long id, @RequestBody UpdateTagDTO updateTagDTO) {
        TagDTO updatedTag = tagService.updateTag(id, updateTagDTO);
        return ResponseEntity.ok(updatedTag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}