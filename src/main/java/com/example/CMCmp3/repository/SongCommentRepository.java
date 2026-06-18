package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.SongComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongCommentRepository extends JpaRepository<SongComment, Long> {

    Page<SongComment> findBySongId(Long songId, Pageable pageable);

    Page<SongComment> findBySongIdAndStatus(
            Long songId,
            SongComment.CommentStatus status,
            Pageable pageable
    );

    Page<SongComment> findByStatus(
            SongComment.CommentStatus status,
            Pageable pageable
    );
}
