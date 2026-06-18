package com.example.CMCmp3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private List<Long> songs;
    private List<ArtistDTO> artists; // New field for associated artists
    private Long playCount;
    private Long likeCount;
    private Long commentCount;
    private String privacy; // Thêm trường privacy
    private int songCount; // Số lượng bài hát trong playlist
    private OwnerDTO owner; // Tên người tạo
    private LocalDateTime createdAt;
    private boolean likedByCurrentUser;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDTO {
        private Long id;
        private String name;
    }
}