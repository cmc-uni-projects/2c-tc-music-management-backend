package com.example.CMCmp3.dto;

import com.example.CMCmp3.entity.ArtistVerificationRequest;
import com.example.CMCmp3.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistRequestDTO {
    private Long id;
    private SimpleUserDTO user;
    private String artistName;
    private String imageUrl;
    private RequestStatus status;
    private LocalDateTime requestDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserDTO {
        private Long id;
        private String username;
        private String displayName;
        private String email;
    }

    public static ArtistRequestDTO fromEntity(ArtistVerificationRequest entity) {
        SimpleUserDTO userDTO = new SimpleUserDTO(
                entity.getUser().getId(),
                entity.getUser().getUsername(),
                entity.getUser().getDisplayName(),
                entity.getUser().getEmail()
        );

        return new ArtistRequestDTO(
                entity.getId(),
                userDTO,
                entity.getArtistName(),
                entity.getImageUrl(),
                entity.getStatus(),
                entity.getRequestDate()
        );
    }
}
