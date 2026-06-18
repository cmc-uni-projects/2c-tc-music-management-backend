package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.AlbumComment;
import com.example.CMCmp3.entity.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumCommentRepository extends JpaRepository<AlbumComment, Long> {

    Page<AlbumComment> findByAlbumId(Long albumId, Pageable pageable);

    Page<AlbumComment> findByAlbumIdAndStatus(
            Long albumId,
            CommentStatus status,
            Pageable pageable
    );

    Page<AlbumComment> findByStatus(
            CommentStatus status,
            Pageable pageable
    );
}
