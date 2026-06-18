package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song reportedSong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist reportedPlaylist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_comment_id")
    private SongComment reportedSongComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_comment_id")
    private PlaylistComment reportedPlaylistComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // PENDING, APPROVED, REJECTED

    // Enum for report type
    public enum ReportType {
        SONG,
        PLAYLIST,
        SONG_COMMENT,
        PLAYLIST_COMMENT
    }

    // Enum for report status
    public enum ReportStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
