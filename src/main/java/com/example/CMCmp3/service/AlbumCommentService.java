package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.CreateAlbumCommentDTO;
import com.example.CMCmp3.dto.AlbumCommentDTO;
import com.example.CMCmp3.dto.UpdateAlbumCommentDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.AlbumCommentRepository;
import com.example.CMCmp3.repository.AlbumRepository;
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
public class AlbumCommentService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final AlbumCommentRepository albumCommentRepository;
    private final NotificationService notificationService;

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

    private AlbumCommentDTO toDTO(AlbumComment comment) {
        return new AlbumCommentDTO(
                comment.getId(),
                comment.getAlbum().getId(),
                comment.getAlbum().getTitle(),
                comment.getContent(),
                comment.getCreatedAt(),
                AlbumCommentDTO.fromUser(comment.getAuthor())
        );
    }

    @Transactional
    public AlbumCommentDTO addCommentToAlbum(Long albumId, CreateAlbumCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        AlbumComment newComment = AlbumComment.builder()
                .album(album)
                .author(currentUser)
                .content(commentDTO.getContent())
                .status(CommentStatus.PENDING)
                .build();

        AlbumComment savedComment = albumCommentRepository.save(newComment);
        return toDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<AlbumCommentDTO> getCommentsByAlbumId(Long albumId, Pageable pageable) {
        if (!albumRepository.existsById(albumId)) {
            throw new NoSuchElementException("Album not found with ID: " + albumId);
        }

        return albumCommentRepository.findByAlbumIdAndStatus(
                albumId,
                CommentStatus.APPROVED,
                pageable
        ).map(this::toDTO);
    }

    @Transactional
    public AlbumCommentDTO updateComment(Long commentId, UpdateAlbumCommentDTO commentDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        AlbumComment comment = albumCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        AlbumComment updatedComment = albumCommentRepository.save(comment);
        return toDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        AlbumComment comment = albumCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        boolean isOwner =
                comment.getAuthor() != null &&
                        comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to delete this comment");
        }

        Album album = comment.getAlbum();
        if (album != null) {
            long currentCommentCount =
                    album.getCommentCount() != null
                            ? album.getCommentCount()
                            : 0L;

            album.setCommentCount(Math.max(0, currentCommentCount - 1));
            albumRepository.save(album);
        }

        albumCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Page<AlbumCommentDTO> getPendingComments(Pageable pageable) {
        return albumCommentRepository.findByStatus(
                CommentStatus.PENDING,
                pageable
        ).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumCommentDTO> getPendingCommentsByAlbumId(Long albumId, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        boolean isOwner =
                album.getOwner() != null &&
                        album.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to view pending comments for this album");
        }

        return albumCommentRepository.findByAlbumIdAndStatus(
                albumId,
                CommentStatus.PENDING,
                pageable
        ).map(this::toDTO);
    }

    @Transactional
    public void approveComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        AlbumComment comment = albumCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        Album album = comment.getAlbum();

        boolean isOwner =
                album.getOwner() != null &&
                        album.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to approve this comment");
        }

        comment.setStatus(CommentStatus.APPROVED);

        long currentCommentCount =
                album.getCommentCount() != null
                        ? album.getCommentCount()
                        : 0L;

        album.setCommentCount(currentCommentCount + 1);
        albumRepository.save(album);

        if (album.getOwner() != null) {
            User commentAuthor = comment.getAuthor();

            notificationService.createAndSendNotification(
                    commentAuthor,
                    album.getOwner(),
                    NotificationType.COMMENT_ALBUM,
                    commentAuthor.getDisplayName()
                            + " đã bình luận album: " + album.getTitle(),
                    album.getId()
            );
        }

        albumCommentRepository.save(comment);
    }

    @Transactional
    public void rejectComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();

        AlbumComment comment = albumCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with ID: " + commentId));

        Album album = comment.getAlbum();

        boolean isOwner =
                album.getOwner() != null &&
                        album.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("User is not authorized to reject this comment");
        }

        comment.setStatus(CommentStatus.REJECTED);
        albumCommentRepository.save(comment);
    }
}
