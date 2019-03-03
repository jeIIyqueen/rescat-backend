package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.log.ApprovalLog;
import org.springframework.data.repository.CrudRepository;

public interface ApprovalLogRepository extends CrudRepository<ApprovalLog, Long> {
}
