package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.PlaylistLike;
import com.example.CMCmp3.entity.PlaylistLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, PlaylistLikeId> {
}
