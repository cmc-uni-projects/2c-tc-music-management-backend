package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;     // ✅ dùng author thống nhất với service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ ENUM LỒNG – BẮT BUỘC PHẢI CÓ
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

    public enum CommentStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
