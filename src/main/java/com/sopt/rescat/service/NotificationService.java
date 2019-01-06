package com.sopt.rescat.service;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.UserNotificationLog;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.NotificationRepository;
import com.sopt.rescat.repository.UserNotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private UserNotificationLogRepository userNotificationLogRepository;
    private NotificationRepository notificationRepository;

    public NotificationService(final UserNotificationLogRepository userNotificationLogRepository,
                               final NotificationRepository notificationRepository) {
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public List<UserNotificationLog> getNotification(User user) {

        List<UserNotificationLog> userNotificationLogs = userNotificationLogRepository.findByReceivingUserOrderByCreatedAtDesc(user);
        userNotificationLogs.stream()
                .filter(userNotificationLog -> userNotificationLog.getNotification().isTargetIdxNull())
                .forEach(userNotificationLog -> userNotificationLog.updateIsChecked());

        return userNotificationLogs;
    }

    @Transactional
    public Notification updateIsChecked(Long idx, User user) {

        Notification notification = notificationRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 알림이 존재하지 않습니다."));

        userNotificationLogRepository.findByNotificationAndReceivingUser(notification,user).updateIsChecked();

        return notification;
    }
}