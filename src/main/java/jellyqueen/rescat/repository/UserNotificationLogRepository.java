package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Notification;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.log.UserNotificationLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserNotificationLogRepository extends CrudRepository<UserNotificationLog, Long> {
    List<UserNotificationLog> findByReceivingUserOrderByCreatedAtDesc(User user);

    UserNotificationLog findByNotificationAndReceivingUser(Notification notification, User user);
}
