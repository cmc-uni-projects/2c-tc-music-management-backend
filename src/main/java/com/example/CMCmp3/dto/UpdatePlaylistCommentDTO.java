package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePlaylistCommentDTO {
    @NotBlank(message = "Comment content cannot be blank")
    private String content;
}
