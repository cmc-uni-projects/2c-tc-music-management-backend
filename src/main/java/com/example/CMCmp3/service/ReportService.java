package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.CreateReportDTO;
import com.example.CMCmp3.dto.ReportDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final SongCommentRepository songCommentRepository;
    private final PlaylistCommentRepository playlistCommentRepository;

    // ==============================
    // Lấy user đang đăng nhập
    // ==============================
    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Current user not found."));
    }

    // ==============================
    // Convert Report -> ReportDTO
    // ==============================
    public ReportDTO toDTO(Report report) {
        System.out.println("Processing report ID: " + report.getId() + ", Type: " + report.getReportType());

        ReportDTO.ReportDTOBuilder dtoBuilder = ReportDTO.builder()
                .id(report.getId())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .reportType(report.getReportType())
                .status(report.getStatus());

        // Reporter
        if (report.getReporter() != null) {
            dtoBuilder.reporter(new ReportDTO.ReporterDTO(
                    report.getReporter().getId(),
                    report.getReporter().getDisplayName()
            ));
        }

        // Song
        if (report.getReportedSong() != null) {
            System.out.println("  - Reported Song: " + report.getReportedSong().getTitle());
            dtoBuilder.reportedSong(new ReportDTO.ReportedSongDTO(
                    report.getReportedSong().getId(),
                    report.getReportedSong().getTitle(),
                    report.getReportedSong().getImageUrl(),
                    report.getReportedSong().getArtists() != null
                            && !report.getReportedSong().getArtists().isEmpty()
                            ? report.getReportedSong().getArtists().stream()
                            .map(artist -> artist.getName())
                            .collect(Collectors.joining(", "))
                            : "Unknown Artist"
            ));
        } else {
            System.out.println("  - Reported Song: NULL");
        }

        // Playlist
        if (report.getReportedPlaylist() != null) {
            System.out.println("  - Reported Playlist: " + report.getReportedPlaylist().getTitle());
            dtoBuilder.reportedPlaylist(new ReportDTO.ReportedPlaylistDTO(
                    report.getReportedPlaylist().getId(),
                    report.getReportedPlaylist().getTitle(),
                    report.getReportedPlaylist().getImageUrl(),
                    report.getReportedPlaylist().getOwner() != null
                            ? report.getReportedPlaylist().getOwner().getDisplayName()
                            : "Unknown Creator"
            ));
        } else {
            System.out.println("  - Reported Playlist: NULL");
        }

        // Song comment
        if (report.getReportedSongComment() != null) {
            System.out.println("  - Reported Song Comment: " + report.getReportedSongComment().getId());

            String authorName =
                    (report.getReportedSongComment().getAuthor() != null)
                            ? report.getReportedSongComment().getAuthor().getDisplayName()
                            : "Unknown User";

            dtoBuilder.reportedSongComment(new ReportDTO.ReportedSongCommentDTO(
                    report.getReportedSongComment().getId(),
                    report.getReportedSongComment().getContent(),
                    authorName
            ));
        } else {
            System.out.println("  - Reported Song Comment: NULL");
        }

        // Playlist comment
        if (report.getReportedPlaylistComment() != null) {
            System.out.println("  - Reported Playlist Comment: " + report.getReportedPlaylistComment().getId());

            String authorName =
                    (report.getReportedPlaylistComment().getAuthor() != null)
                            ? report.getReportedPlaylistComment().getAuthor().getDisplayName()
                            : "Unknown User";

            dtoBuilder.reportedPlaylistComment(new ReportDTO.ReportedPlaylistCommentDTO(
                    report.getReportedPlaylistComment().getId(),
                    report.getReportedPlaylistComment().getContent(),
                    authorName
            ));
        } else {
            System.out.println("  - Reported Playlist Comment: NULL");
        }

        return dtoBuilder.build();
    }

    // ==============================
    // Lấy tất cả report PENDING
    // ==============================
    @Transactional(readOnly = true)
    public List<ReportDTO> getPendingReports() {
        return reportRepository.findAllByStatus(Report.ReportStatus.PENDING)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==============================
    // APPROVE REPORT
    // ==============================
    @Transactional
    public void approveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found with ID: " + reportId));

        // Chỉ xử lý report đang PENDING
        if (report.getStatus() != Report.ReportStatus.PENDING) {
            return;
        }

        Report.ReportType type = report.getReportType();

        switch (type) {
            case SONG_COMMENT -> {
                SongComment sc = report.getReportedSongComment();
                if (sc != null) {
                    // Ẩn / từ chối comment bài hát
                    sc.setStatus(SongComment.CommentStatus.REJECTED);
                    songCommentRepository.save(sc);
                }
            }
            case PLAYLIST_COMMENT -> {
                PlaylistComment pc = report.getReportedPlaylistComment();
                if (pc != null) {
                    // Ẩn / từ chối comment playlist
                    pc.setStatus(PlaylistComment.CommentStatus.REJECTED);
                    playlistCommentRepository.save(pc);
                }
            }
            case SONG -> {
                // TODO: nếu muốn block bài hát, thêm field isBlocked trong Song và xử lý ở đây
            }
            case PLAYLIST -> {
                // TODO: tương tự cho Playlist
            }
        }

        // Đánh dấu report đã được chấp nhận
        report.setStatus(Report.ReportStatus.APPROVED);
        reportRepository.save(report);
    }

    // ==============================
    // REJECT REPORT
    // ==============================
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found with ID: " + reportId));

        report.setStatus(Report.ReportStatus.REJECTED);
        reportRepository.save(report);
    }

    // ==============================
    // Tạo report mới
    // ==============================
    @Transactional
    public ReportDTO createReport(CreateReportDTO dto) {
        User reporter = getCurrentAuthenticatedUser();

        Report.ReportType reportType;
        Song reportedSong = null;
        Playlist reportedPlaylist = null;
        SongComment reportedSongComment = null;
        PlaylistComment reportedPlaylistComment = null;

        try {
            reportType = Report.ReportType.valueOf(dto.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid entityType: " + dto.getEntityType()
                            + ". Must be one of SONG, PLAYLIST, SONG_COMMENT, PLAYLIST_COMMENT."
            );
        }

        switch (reportType) {
            case SONG -> reportedSong = songRepository.findById(dto.getEntityId())
                    .orElseThrow(() -> new NoSuchElementException("Song not found with ID: " + dto.getEntityId()));
            case PLAYLIST -> reportedPlaylist = playlistRepository.findById(dto.getEntityId())
                    .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + dto.getEntityId()));
            case SONG_COMMENT -> reportedSongComment = songCommentRepository.findById(dto.getEntityId())
                    .orElseThrow(() -> new NoSuchElementException("Song comment not found with ID: " + dto.getEntityId()));
            case PLAYLIST_COMMENT -> reportedPlaylistComment = playlistCommentRepository.findById(dto.getEntityId())
                    .orElseThrow(() -> new NoSuchElementException("Playlist comment not found with ID: " + dto.getEntityId()));
        }

        Report newReport = Report.builder()
                .reason(dto.getReason())
                .reporter(reporter)
                .reportType(reportType)
                .reportedSong(reportedSong)
                .reportedPlaylist(reportedPlaylist)
                .reportedSongComment(reportedSongComment)
                .reportedPlaylistComment(reportedPlaylistComment)
                .status(Report.ReportStatus.PENDING)
                .build();

        newReport = reportRepository.save(newReport);
        return toDTO(newReport);
    }
}
