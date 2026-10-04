package com.fieldops.notification;

import com.fieldops.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications(Boolean unreadOnly) {
        List<Notification> list = (Boolean.TRUE.equals(unreadOnly)) ?
                notificationRepository.findByReadStatusOrderByCreatedAtDesc(false) :
                notificationRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(NotificationDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadStatusFalse();
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        n.setReadStatus(true);
        return NotificationDTO.fromEntity(notificationRepository.save(n));
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByReadStatusOrderByCreatedAtDesc(false);
        for (Notification n : unread) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(unread);
    }
}
