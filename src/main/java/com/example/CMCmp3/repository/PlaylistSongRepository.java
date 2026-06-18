package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.PlaylistSong;
import com.example.CMCmp3.entity.PlaylistSongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlaylistSong ps WHERE ps.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);
}

