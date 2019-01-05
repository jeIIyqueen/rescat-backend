package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification,Long> {
    List<Notification> findByReceivingUserOrderByCreatedAtDesc(User user);
}
