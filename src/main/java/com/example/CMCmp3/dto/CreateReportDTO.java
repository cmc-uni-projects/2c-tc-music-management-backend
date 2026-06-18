package com.example.CMCmp3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportDTO {
    @NotBlank(message = "Entity type cannot be blank")
    private String entityType; // e.g., "SONG", "PLAYLIST"

    @NotNull(message = "Entity ID cannot be null")
    @Positive(message = "Entity ID must be positive")
    private Long entityId;

    @NotBlank(message = "Reason cannot be blank")
    private String reason;
}
