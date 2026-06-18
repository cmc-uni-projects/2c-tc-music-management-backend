package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumLike {

    @EmbeddedId
    private AlbumLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("albumId")
    @JoinColumn(name = "album_id")
    private Album album;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
