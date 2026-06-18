package com.example.CMCmp3.dto;

import com.example.CMCmp3.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongCommentDTO {
    private Long id;
    private Long songId;
    private String songTitle;
    private String content;
    private LocalDateTime createdAt;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String avatarUrl;
    }

    public static UserInfo fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserInfo(user.getId(), user.getDisplayName(), user.getAvatarUrl());
    }
}
