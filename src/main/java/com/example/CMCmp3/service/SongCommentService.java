package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.CreateSongCommentDTO;
import com.example.CMCmp3.dto.SongCommentDTO;
import com.example.CMCmp3.dto.UpdateSongCommentDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.SongCommentRepository;
import com.example.CMCmp3.repository.SongRepository;
import com.example.CMCmp3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SongCommentService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongCommentRepository songCommentRepository;
    private final NotificationService notificationService;

    // ==========================
    // Lấy user đang đăng nhập
    // ==========================
    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Current user not found in database"));
    }

    // ==========================
    // Convert entity -> DTO
    // ==========================
    private SongCommentDTO toDTO(SongComment comment) {
        return new SongCommentDTO(
                comment.getId(),
                comment.getSong().getId(),
                comment.getSong().getTitle(),
                comment.getContent(),
                comment.getCreatedAt(),
                SongCommentDTO.fromUser(comment.getAuthor()) // ✅ author
        );
    }

    // ==========================
    // Thêm bình luận vào bài hát
    // ==========================
    @Transactional
    public SongCommentDTO addCommentToSong(Long songId, CreateSongCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new NoSuchElementException("Song not found with ID: " + songId));

        SongComment newComment = SongComment.builder()
                .song(song)
                .author(currentUser)                         // ✅ author
                .content(commentDTO.getContent())
                .status(SongComment.CommentStatus.PENDING)   // ✅ enum lồng
                .build();

        SongComment savedComment = songCommentRepository.save(newComment);
        return toDTO(savedComment);
    }

    // ==========================
    // Lấy comment APPROVED của 1 bài hát
    // ==========================
    @Transactional(readOnly = true)
    public Page<SongCommentDTO> getCommentsBySongId(Long songId, Pageable pageable) {
        if (!songRepository.existsById(songId)) {
            throw new NoSuchElementException("Song not found with ID: " + songId);
        }

        return songCommentRepository.findBySongIdAndStatus(
                songId,
                SongComment.CommentStatus.APPROVED,   // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Cập nhật bình luận
    // ==========================
    @Transactional
    public SongCommentDTO updateComment(Long commentId, UpdateSongCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        SongComment updatedComment = songCommentRepository.save(comment);
        return toDTO(updatedComment);
    }

    // ==========================
    // Xoá bình luận
    // ==========================
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to delete this comment");
        }

        Song song = comment.getSong();
        if (song != null) {
            song.setCommentCount(Math.max(0, song.getCommentCount() - 1));
            songRepository.save(song);
        }

        songCommentRepository.delete(comment);
    }

    // ==========================
    // Lấy comment PENDING (chờ duyệt)
    // ==========================
    @Transactional(readOnly = true)
    public Page<SongCommentDTO> getPendingComments(Pageable pageable) {
        return songCommentRepository.findByStatus(
                SongComment.CommentStatus.PENDING,  // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Lấy comment PENDING theo bài hát (uploader hoặc admin xem)
    // ==========================
    @Transactional(readOnly = true)
    public Page<SongCommentDTO> getPendingCommentsBySongId(Long songId, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new NoSuchElementException("Song not found with ID: " + songId));

        boolean isOwner =
                song.getUploader() != null &&
                        song.getUploader().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to view pending comments for this song");
        }

        return songCommentRepository.findBySongIdAndStatus(
                songId,
                SongComment.CommentStatus.PENDING,   // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Duyệt comment (APPROVE)
    // ==========================
    @Transactional
    public void approveComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NoSuchElementException("Comment not found with ID: " + commentId));

        Song song = comment.getSong();

        boolean isOwner =
                song.getUploader() != null &&
                        song.getUploader().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to approve this comment");
        }

        comment.setStatus(SongComment.CommentStatus.APPROVED);

        song.setCommentCount(song.getCommentCount() + 1);
        songRepository.save(song);

        if (song.getUploader() != null) {
            notificationService.createAndSendNotification(
                    comment.getAuthor(),                   // ✅ sender = author
                    song.getUploader(),                    // recipient
                    NotificationType.COMMENT_SONG,
                    comment.getAuthor().getDisplayName()
                            + " đã bình luận bài hát: " + song.getTitle(),
                    song.getId()
            );
        }

        songCommentRepository.save(comment);
    }

    // ==========================
    // Từ chối comment (REJECT)
    // ==========================
    @Transactional
    public void rejectComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NoSuchElementException("Comment not found with ID: " + commentId));

        Song song = comment.getSong();

        boolean isOwner =
                song.getUploader() != null &&
                        song.getUploader().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to reject this comment");
        }

        comment.setStatus(SongComment.CommentStatus.REJECTED);
        songCommentRepository.save(comment);
    }
}
