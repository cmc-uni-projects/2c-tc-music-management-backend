package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy thông báo của user, sắp xếp mới nhất lên đầu
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Đếm số thông báo chưa đọc (để hiển thị số đỏ trên icon)
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}
