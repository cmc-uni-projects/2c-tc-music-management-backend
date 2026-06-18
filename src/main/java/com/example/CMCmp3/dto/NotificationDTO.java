package com.example.CMCmp3.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String senderName;
    private String senderAvatar;
    private String message;
    private String type;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
