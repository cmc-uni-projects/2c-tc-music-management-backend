package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "song_listen_logs")
public class SongListenLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // Nullable để cho phép người dùng không đăng nhập
    private User user;

    @Column(nullable = false)
    private LocalDateTime listenTimestamp;

    public SongListenLog(Song song, User user, LocalDateTime listenTimestamp) {
        this.song = song;
        this.user = user;
        this.listenTimestamp = listenTimestamp;
    }
}
