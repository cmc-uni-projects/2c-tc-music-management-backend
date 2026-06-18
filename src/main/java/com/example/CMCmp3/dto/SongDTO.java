package com.example.CMCmp3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class SongDTO {

    // 1. Đổi String -> Long để khớp với Entity và CSDL MySQL
    private Long id;

    private String title;

    private String artistName; // Thêm trường artistName

    // 2. Thêm thuộc tính duration (Tính bằng giây)
    private Integer duration;

    // 3. Đổi từ "String artist" -> Danh sách đối tượng Artist (để lấy cả ảnh, id của ca sĩ)
    private Set<ArtistDTO> artists;

    // 4. Đổi từ "label" -> Danh sách Tag (Thể loại)
    private Set<TagDTO> tags;

    private String imageUrl;

    private String filePath;

    private Long listenCount;

    private Long likeCount;

    private String description;

    private List<LyricLineDTO> lyrics;

    // Dùng LocalDateTime để dễ xử lý hơn Instant trong các query thông thường
    private LocalDateTime createdAt;

    private UploaderDTO uploader;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploaderDTO {
        private Long id;
        private String name;
    }
}