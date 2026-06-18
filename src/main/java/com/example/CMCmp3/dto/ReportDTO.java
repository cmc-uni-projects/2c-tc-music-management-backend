package com.example.CMCmp3.dto;

import com.example.CMCmp3.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long id;
    private String reason;
    private LocalDateTime createdAt;
    private ReporterDTO reporter;
    private Report.ReportType reportType;
    private ReportedSongDTO reportedSong;
    private ReportedPlaylistDTO reportedPlaylist;
    private ReportedSongCommentDTO reportedSongComment;
    private ReportedPlaylistCommentDTO reportedPlaylistComment;
    private Report.ReportStatus status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporterDTO {
        private Long id;
        private String displayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedSongDTO {
        private Long id;
        private String title;
        private String imageUrl;
        private String artistName; // For display convenience
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedPlaylistDTO {
        private Long id;
        private String name;
        private String imageUrl;
        private String creatorName; // For display convenience
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedSongCommentDTO {
        private Long id;
        private String content;
        private String authorName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportedPlaylistCommentDTO {
        private Long id;
        private String content;
        private String authorName;
    }
}
