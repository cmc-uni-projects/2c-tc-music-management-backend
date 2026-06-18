package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "song_likes")
public class SongLike {

    @EmbeddedId
    private SongLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @CreationTimestamp
    private LocalDateTime likedAt;

    public SongLike(User user, Song song) {
        this.user = user;
        this.song = song;
        this.id = new SongLikeId(user.getId(), song.getId());
    }
}
