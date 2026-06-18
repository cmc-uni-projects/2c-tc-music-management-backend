package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.CreateArtistRequestDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.ArtistRepository;
import com.example.CMCmp3.repository.ArtistVerificationRequestRepository;
import com.example.CMCmp3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistVerificationService {

    private final ArtistVerificationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    // =========================
    // USER: gửi yêu cầu xác thực
    // =========================
    @Transactional
    public ArtistVerificationRequest createRequest(CreateArtistRequestDTO dto, User user) {
        if (user == null) throw new AccessDeniedException("Chưa đăng nhập.");

        // ✅ validate tối thiểu
        if (dto == null
                || !StringUtils.hasText(dto.getArtistName())
                || !StringUtils.hasText(dto.getImageUrl())) {
            throw new IllegalArgumentException("Thiếu thông tin artistName hoặc imageUrl.");
        }

        // ✅ luôn lấy user managed từ DB
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Nếu đã được duyệt rồi thì không cho gửi nữa
        if (managedUser.getArtistVerificationStatus() == ArtistVerificationStatus.APPROVED) {
            throw new IllegalStateException("Bạn đã được xác thực nghệ sĩ.");
        }

        // ✅ Nếu đang PENDING thì không cho gửi tiếp
        boolean hasPendingRequest = requestRepository.existsByUserIdAndStatus(managedUser.getId(), RequestStatus.PENDING);
        if (managedUser.getArtistVerificationStatus() == ArtistVerificationStatus.PENDING || hasPendingRequest) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu, vui lòng chờ admin duyệt.");
        }

        // ✅ Tạo request (status/requestDate set bởi @PrePersist)
        ArtistVerificationRequest request = new ArtistVerificationRequest();
        request.setUser(managedUser);
        request.setArtistName(dto.getArtistName().trim());
        request.setImageUrl(dto.getImageUrl().trim());

        ArtistVerificationRequest saved = requestRepository.save(request);

        // ✅ HƯỚNG A: cập nhật trạng thái user -> PENDING
        managedUser.setArtistVerificationStatus(ArtistVerificationStatus.PENDING);
        userRepository.save(managedUser);

        return saved;
    }

    // =========================
    // ADMIN: xem danh sách pending
    // =========================
    @Transactional(readOnly = true)
    public List<ArtistVerificationRequest> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING);
        // nếu có:
        // return requestRepository.findByStatusOrderByRequestDateDesc(RequestStatus.PENDING);
    }

    // =========================
    // ADMIN: duyệt yêu cầu
    // =========================
    @Transactional
    public void approveRequest(Long requestId, User admin) {
        if (admin == null) throw new AccessDeniedException("Chưa đăng nhập.");

        User managedAdmin = userRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (managedAdmin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ ADMIN mới được duyệt xác thực nghệ sĩ.");
        }

        ArtistVerificationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Nếu đã APPROVED rồi thì bỏ qua (idempotent)
        if (user.getArtistVerificationStatus() == ArtistVerificationStatus.APPROVED) {
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);
            return;
        }

        // ✅ Nếu đã có Artist thì update, chưa có thì tạo mới
        Artist artist = user.getArtist();
        if (artist == null) artist = new Artist();

        artist.setName(request.getArtistName());
        artist.setImageUrl(request.getImageUrl());
        Artist savedArtist = artistRepository.save(artist);

        // ✅ Update User
        user.setArtist(savedArtist);

        // ✅ Nếu user không phải admin thì set role = ARTIST
        if (user.getRole() != Role.ADMIN) {
            user.setRole(Role.ARTIST);
        }

        // ✅ HƯỚNG A: set trạng thái xác thực
        user.setArtistVerificationStatus(ArtistVerificationStatus.APPROVED);

        userRepository.save(user);

        // ✅ Update Request status
        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);
    }

    // =========================
    // ADMIN: từ chối yêu cầu
    // =========================
    @Transactional
    public void rejectRequest(Long requestId, User admin) {
        if (admin == null) throw new AccessDeniedException("Chưa đăng nhập.");

        User managedAdmin = userRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (managedAdmin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ ADMIN mới được từ chối xác thực nghệ sĩ.");
        }

        ArtistVerificationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ HƯỚNG A: cập nhật trạng thái user -> REJECTED
        user.setArtistVerificationStatus(ArtistVerificationStatus.REJECTED);
        userRepository.save(user);

        // ✅ Update Request status
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);
    }
}
