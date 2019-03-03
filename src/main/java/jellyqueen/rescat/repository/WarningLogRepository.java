package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.log.WarningLog;
import jellyqueen.rescat.domain.enums.WarningType;
import org.springframework.data.repository.CrudRepository;

public interface WarningLogRepository extends CrudRepository<WarningLog, Long> {
    boolean existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(Long carePostIdx, WarningType warningType, User user);
}
