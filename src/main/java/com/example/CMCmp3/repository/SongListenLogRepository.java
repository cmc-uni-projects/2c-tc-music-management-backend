package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.SongListenLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SongListenLogRepository extends JpaRepository<SongListenLog, Long> {

    /**
     * Đếm số lượt nghe của các bài hát có tag nhất định trong một khoảng thời gian.
     */
    @Query("""
           SELECT COUNT(sll.id)
           FROM SongListenLog sll
           JOIN sll.song s
           JOIN s.tags t
           WHERE sll.listenTimestamp BETWEEN :startTime AND :endTime
             AND t.name = :tagName
           """)
    long countListensBetweenWithTag(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("tagName") String tagName
    );

    /**
     * Đếm số lượt nghe của một bài hát cụ thể trong một khoảng thời gian.
     */
    @Query("""
           SELECT COUNT(sll.id)
           FROM SongListenLog sll
           WHERE sll.song.id = :songId
             AND sll.listenTimestamp BETWEEN :startTime AND :endTime
           """)
    long countListensForSongBetween(
            @Param("songId") Long songId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /* ================== THÊM MỚI CHO ZINGCHART ================== */

    /**
     * Lấy top bài hát theo số lượt nghe trong khoảng thời gian [startTime, endTime].
     * Dùng Pageable để giới hạn số lượng (ví dụ top 3).
     */
    @Query("""
           SELECT sll.song.id   AS songId,
                  COUNT(sll.id) AS listenCount
           FROM SongListenLog sll
           WHERE sll.listenTimestamp BETWEEN :startTime AND :endTime
           GROUP BY sll.song.id
           ORDER BY listenCount DESC
           """)
    List<TopSongCount> findTopSongsBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * Projection cho kết quả top bài hát.
     */
    interface TopSongCount {
        Long getSongId();
        Long getListenCount();
    }

    @Query("SELECT a.id FROM SongListenLog sll JOIN sll.song s JOIN s.artists a WHERE sll.user.id = :userId GROUP BY a.id ORDER BY COUNT(sll) DESC")
    List<Long> findTopArtistIdsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t.id FROM SongListenLog sll JOIN sll.song s JOIN s.tags t WHERE sll.user.id = :userId GROUP BY t.id ORDER BY COUNT(sll) DESC")
    List<Long> findTopTagIdsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s.id FROM SongListenLog sll JOIN sll.song s WHERE sll.user.id = :userId")
    List<Long> findListenedSongIdsByUserId(@Param("userId") Long userId);
}
