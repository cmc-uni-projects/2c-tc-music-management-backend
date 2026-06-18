package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "song_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)   // cột DB vẫn là user_id
    private User author;                              // nhưng field là author

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

    public enum CommentStatus {
        PENDING,    // mới tạo, chờ duyệt
        APPROVED,   // đã duyệt, được hiển thị
        REJECTED    // bị từ chối
    }
}
