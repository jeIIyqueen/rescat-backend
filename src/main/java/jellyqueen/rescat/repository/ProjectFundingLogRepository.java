package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Funding;
import jellyqueen.rescat.domain.log.ProjectFundingLog;
import jellyqueen.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectFundingLogRepository extends CrudRepository<ProjectFundingLog, Long> {
    List<ProjectFundingLog> findBySponsorOrderByCreatedAtDesc(User user);

    List<ProjectFundingLog> findByFunding(Funding funding);
}
