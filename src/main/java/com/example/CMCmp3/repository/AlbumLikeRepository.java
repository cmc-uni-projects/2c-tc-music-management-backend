package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.AlbumLike;
import com.example.CMCmp3.entity.AlbumLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumLikeRepository extends JpaRepository<AlbumLike, AlbumLikeId> {
}
