package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.CreatePlaylistCommentDTO;
import com.example.CMCmp3.dto.PlaylistCommentDTO;
import com.example.CMCmp3.dto.UpdatePlaylistCommentDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.PlaylistCommentRepository;
import com.example.CMCmp3.repository.PlaylistRepository;
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
public class PlaylistCommentService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistCommentRepository playlistCommentRepository;
    private final NotificationService notificationService;

    // ==========================
    // Lấy user đang đăng nhập
    // ==========================
    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    // ==========================
    // Convert entity -> DTO
    // ==========================
    private PlaylistCommentDTO toDTO(PlaylistComment comment) {
        return new PlaylistCommentDTO(
                comment.getId(),
                comment.getPlaylist().getId(),
                comment.getPlaylist().getTitle(),
                comment.getContent(),
                comment.getCreatedAt(),
                PlaylistCommentDTO.fromUser(comment.getAuthor())   // ✅ author
        );
    }

    // ==========================
    // Thêm comment cho playlist
    // ==========================
    @Transactional
    public PlaylistCommentDTO addCommentToPlaylist(Long playlistId, CreatePlaylistCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        PlaylistComment newComment = PlaylistComment.builder()
                .playlist(playlist)
                .author(currentUser)                                     // ✅ author
                .content(commentDTO.getContent())
                .status(PlaylistComment.CommentStatus.PENDING)          // ✅ enum lồng
                .build();

        PlaylistComment savedComment = playlistCommentRepository.save(newComment);
        return toDTO(savedComment);
    }

    // ==========================
    // Lấy comment APPROVED của playlist
    // ==========================
    @Transactional(readOnly = true)
    public Page<PlaylistCommentDTO> getCommentsByPlaylistId(Long playlistId, Pageable pageable) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new NoSuchElementException("Playlist not found with ID: " + playlistId);
        }

        return playlistCommentRepository.findByPlaylistIdAndStatus(
                playlistId,
                PlaylistComment.CommentStatus.APPROVED,   // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Cập nhật comment
    // ==========================
    @Transactional
    public PlaylistCommentDTO updateComment(Long commentId, UpdatePlaylistCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        PlaylistComment comment = playlistCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        PlaylistComment updatedComment = playlistCommentRepository.save(comment);
        return toDTO(updatedComment);
    }

    // ==========================
    // Xoá comment
    // ==========================
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        PlaylistComment comment = playlistCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to delete this comment");
        }

        Playlist playlist = comment.getPlaylist();
        if (playlist != null) {
            long currentCommentCount =
                    playlist.getCommentCount() != null
                            ? playlist.getCommentCount()
                            : 0L;

            playlist.setCommentCount(Math.max(0, currentCommentCount - 1));
            playlistRepository.save(playlist);
        }

        playlistCommentRepository.delete(comment);
    }

    // ==========================
    // Lấy comment PENDING toàn hệ thống
    // ==========================
    @Transactional(readOnly = true)
    public Page<PlaylistCommentDTO> getPendingComments(Pageable pageable) {
        return playlistCommentRepository.findByStatus(
                PlaylistComment.CommentStatus.PENDING,   // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Lấy comment PENDING theo playlist
    // ==========================
    @Transactional(readOnly = true)
    public Page<PlaylistCommentDTO> getPendingCommentsByPlaylistId(Long playlistId, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        boolean isOwner =
                playlist.getOwner() != null &&
                        playlist.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to view pending comments for this playlist");
        }

        return playlistCommentRepository.findByPlaylistIdAndStatus(
                playlistId,
                PlaylistComment.CommentStatus.PENDING,   // ✅ enum lồng
                pageable
        ).map(this::toDTO);
    }

    // ==========================
    // Duyệt comment (APPROVE)
    // ==========================
    @Transactional
    public void approveComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        PlaylistComment comment = playlistCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        Playlist playlist = comment.getPlaylist();

        boolean isOwner =
                playlist.getOwner() != null &&
                        playlist.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to approve this comment");
        }

        comment.setStatus(PlaylistComment.CommentStatus.APPROVED);

        long currentCommentCount =
                playlist.getCommentCount() != null
                        ? playlist.getCommentCount()
                        : 0L;

        playlist.setCommentCount(currentCommentCount + 1);
        playlistRepository.save(playlist);

        if (playlist.getOwner() != null) {
            User commentAuthor = comment.getAuthor();

            notificationService.createAndSendNotification(
                    commentAuthor,                         // ✅ sender
                    playlist.getOwner(),                  // recipient
                    NotificationType.COMMENT_PLAYLIST,
                    commentAuthor.getDisplayName()
                            + " đã bình luận playlist: " + playlist.getTitle(),
                    playlist.getId()
            );
        }

        playlistCommentRepository.save(comment);
    }

    // ==========================
    // Từ chối comment (REJECT)
    // ==========================
    @Transactional
    public void rejectComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        PlaylistComment comment = playlistCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        Playlist playlist = comment.getPlaylist();

        boolean isOwner =
                playlist.getOwner() != null &&
                        playlist.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to reject this comment");
        }

        comment.setStatus(PlaylistComment.CommentStatus.REJECTED);
        playlistCommentRepository.save(comment);
    }
}
