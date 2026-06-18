package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.ArtistVerificationRequest;
import com.example.CMCmp3.entity.RequestStatus;
import com.example.CMCmp3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistVerificationRequestRepository extends JpaRepository<ArtistVerificationRequest, Long> {

    // ✅ giữ lại: dùng được khi user là entity managed
    boolean existsByUserAndStatus(User user, RequestStatus status);

    // ✅ NEW (khuyên dùng): an toàn hơn vì chỉ cần userId
    boolean existsByUserIdAndStatus(Long userId, RequestStatus status);

    // ✅ list theo status
    List<ArtistVerificationRequest> findByStatus(RequestStatus status);

    // ✅ list pending mới nhất trước (admin dễ duyệt)
    List<ArtistVerificationRequest> findByStatusOrderByRequestDateDesc(RequestStatus status);

    // ✅ lấy request mới nhất của user
    Optional<ArtistVerificationRequest> findTopByUserOrderByRequestDateDesc(User user);

    // ✅ NEW (tuỳ chọn): lấy request mới nhất theo userId (an toàn hơn)
    Optional<ArtistVerificationRequest> findTopByUserIdOrderByRequestDateDesc(Long userId);
}
