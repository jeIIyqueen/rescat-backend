package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
