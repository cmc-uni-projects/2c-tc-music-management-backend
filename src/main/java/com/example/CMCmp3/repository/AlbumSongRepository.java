package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.AlbumSong;
import com.example.CMCmp3.entity.AlbumSongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumSongRepository extends JpaRepository<AlbumSong, AlbumSongId> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AlbumSong als WHERE als.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);
}
