package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.Song;
import com.example.CMCmp3.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByTitleContainingIgnoreCaseAndUploader(String title, User uploader);

    // Custom findAll to eagerly fetch artists and tags
    @Query(value = "SELECT DISTINCT s FROM Song s LEFT JOIN FETCH s.artists LEFT JOIN FETCH s.tags WHERE s.status = com.example.CMCmp3.entity.SongStatus.APPROVED",
           countQuery = "SELECT COUNT(s) FROM Song s WHERE s.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    Page<Song> findAll(Pageable pageable);

    // Custom findById to eagerly fetch artists and tags
    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.artists LEFT JOIN FETCH s.tags WHERE s.id = :id")
    Optional<Song> findById(@Param("id") Long id);

    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.artists LEFT JOIN FETCH s.tags WHERE s.id = :id AND s.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    Optional<Song> findApprovedById(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Song s LEFT JOIN s.artists a WHERE " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND s.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    List<Song> searchSongsByTitleOrArtist(@Param("query") String query);

    List<Song> findByTitleContainingIgnoreCase(String title);

    Optional<Song> findFirstByTitleContainingIgnoreCase(String title);

    List<Song> findAllByArtistsNameContainingIgnoreCase(String artistName);

    @Query("SELECT DISTINCT s FROM Song s JOIN s.artists a JOIN s.tags t WHERE s.id != :songId AND (a.id IN :artistIds OR t.id IN :tagIds)")
    List<Song> findSimilarSongs(@Param("songId") Long songId, @Param("artistIds") Set<Long> artistIds, @Param("tagIds") Set<Long> tagIds, Pageable pageable);

    List<Song> findAllByTagsNameContainingIgnoreCase(String tagName);


    List<Song> findAllByArtistsIdAndStatus(Long artistId, com.example.CMCmp3.entity.SongStatus status);


    @Query("SELECT s FROM Song s WHERE s.status = com.example.CMCmp3.entity.SongStatus.APPROVED ORDER BY s.listenCount DESC")
    List<Song> findTopByListenCount(Pageable pageable);


    @Query("SELECT s FROM Song s WHERE s.status = com.example.CMCmp3.entity.SongStatus.APPROVED ORDER BY s.createdAt DESC")
    List<Song> findTopByCreatedAt(Pageable pageable);


    @Query("SELECT s FROM Song s WHERE s.status = com.example.CMCmp3.entity.SongStatus.APPROVED ORDER BY s.likeCount DESC")
    List<Song> findTopByLikeCount(Pageable pageable);

    List<Song> findByUploader(User uploader);

    @Query("SELECT s FROM Song s WHERE s.uploader.id = :userId AND LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Song> findByUploaderIdAndTitleContaining(@Param("userId") Long userId, @Param("query") String query);

    @Query("SELECT sl.song FROM SongLike sl WHERE sl.user.id = :userId AND sl.song.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    List<Song> findLikedSongsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT DISTINCT s FROM Song s LEFT JOIN FETCH s.artists LEFT JOIN FETCH s.tags WHERE s.status = :status",
            countQuery = "SELECT COUNT(s) FROM Song s WHERE s.status = :status")
    Page<Song> findAllByStatus(@Param("status") com.example.CMCmp3.entity.SongStatus status, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Song s LEFT JOIN FETCH s.artists LEFT JOIN FETCH s.tags JOIN s.lyrics l WHERE " +
            "LOWER(l.text) LIKE LOWER(CONCAT('%', :query, '%')) AND s.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    List<Song> searchByLyric(@Param("query") String query);

    @Query("SELECT DISTINCT s FROM Song s JOIN s.artists a JOIN s.tags t " +
            "WHERE s.id NOT IN :excludedSongIds AND (a.id IN :artistIds OR t.id IN :tagIds) " +
            "AND s.status = com.example.CMCmp3.entity.SongStatus.APPROVED")
    List<Song> findRecommendedSongs(@Param("artistIds") List<Long> artistIds,
                                    @Param("tagIds") List<Long> tagIds,
                                    @Param("excludedSongIds") List<Long> excludedSongIds,
                                    Pageable pageable);
}
