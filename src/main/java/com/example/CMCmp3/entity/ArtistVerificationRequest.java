package com.example.CMCmp3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artist_verification_requests")
public class ArtistVerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String artistName;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        status = RequestStatus.PENDING;
    }
}
