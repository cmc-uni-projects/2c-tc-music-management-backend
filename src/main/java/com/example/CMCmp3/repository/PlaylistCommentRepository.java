package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.PlaylistComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistCommentRepository extends JpaRepository<PlaylistComment, Long> {

    Page<PlaylistComment> findByPlaylistId(Long playlistId, Pageable pageable);

    Page<PlaylistComment> findByPlaylistIdAndStatus(
            Long playlistId,
            PlaylistComment.CommentStatus status,
            Pageable pageable
    );

    Page<PlaylistComment> findByStatus(
            PlaylistComment.CommentStatus status,
            Pageable pageable
    );
}
