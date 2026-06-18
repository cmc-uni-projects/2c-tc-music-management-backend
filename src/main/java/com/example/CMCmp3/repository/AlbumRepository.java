package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.Album;
import com.example.CMCmp3.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByOwner(User owner);

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.owner LEFT JOIN FETCH a.albumSongs als LEFT JOIN FETCH als.song")
    List<Album> findAll();

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.owner LEFT JOIN FETCH a.albumSongs als LEFT JOIN FETCH als.song WHERE a.id = :id")
    Optional<Album> findById(@Param("id") Long id);

    @Query("SELECT a FROM Album a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Album> searchByTitle(@Param("query") String query);

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.owner LEFT JOIN FETCH a.albumSongs als LEFT JOIN FETCH als.song ORDER BY a.playCount DESC")
    List<Album> findTopByPlayCount(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.owner LEFT JOIN FETCH a.albumSongs als LEFT JOIN FETCH als.song ORDER BY a.createdAt DESC")
    List<Album> findTopByCreatedAt(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN FETCH a.owner LEFT JOIN FETCH a.albumSongs als LEFT JOIN FETCH als.song ORDER BY a.likeCount DESC")
    List<Album> findTopByLikeCount(Pageable pageable);

    @Query("SELECT al.album FROM AlbumLike al WHERE al.user.id = :userId")
    List<Album> findLikedAlbumsByUserId(@Param("userId") Long userId);
}
