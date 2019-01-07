package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.UserNotificationLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserNotificationLogRepository extends CrudRepository<UserNotificationLog, Long> {
        List<UserNotificationLog> findByReceivingUserOrderByCreatedAtDesc(User user);
        UserNotificationLog findByNotificationAndReceivingUser(Notification notification, User user);
}
