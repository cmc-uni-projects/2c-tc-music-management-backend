package com.example.CMCmp3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private List<Long> songIds;
    private List<ArtistDTO> artists;
    private Long playCount;
    private Long likeCount;
    private Long commentCount;
    private int songCount;
    private OwnerDTO owner;
    private LocalDateTime createdAt;
    private boolean likedByCurrentUser;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDTO {
        private Long id;
        private String name;
        private String role;
        private boolean isVerifiedArtist;
    }
}
