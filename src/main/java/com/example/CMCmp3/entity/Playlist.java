package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private Long playCount = 0L;
    private Long likeCount = 0L;
    private Long commentCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistPrivacy privacy = PlaylistPrivacy.PRIVATE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // --- CÁC MỐI QUAN HỆ ---

    // Chủ sở hữu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // Danh sách bài hát (QUAN TRỌNG: Dùng bảng trung gian PlaylistSong để lưu thứ tự)
    // Không dùng @ManyToMany trực tiếp được vì cần cột 'position'
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaylistSong> playlistSongs = new HashSet<>();

    // Quan hệ Likes và Comments (Để xóa playlist thì xóa luôn like/comment)
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaylistLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaylistComment> comments = new HashSet<>();

    // Danh sách nghệ sĩ liên quan (ManyToMany)
    @ManyToMany
    @JoinTable(
            name = "playlist_artists",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();
}