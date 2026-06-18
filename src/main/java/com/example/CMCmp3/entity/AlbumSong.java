package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumSong {

    @EmbeddedId
    private AlbumSongId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("albumId") // Map với field albumId trong khóa chính
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId") // Map với field songId trong khóa chính
    @JoinColumn(name = "song_id")
    private Song song;

    // ĐÂY LÀ LÝ DO CHÚNG TA PHẢI TẠO BẢNG NÀY:
    @Column(name = "song_order")
    private Integer order; // Thứ tự bài hát (1, 2, 3...)

    @CreationTimestamp
    private LocalDateTime addedAt; // Ngày thêm bài vào album
}
