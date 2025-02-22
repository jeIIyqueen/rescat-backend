package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Funding;
import jellyqueen.rescat.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FundingRepository extends CrudRepository<Funding, Long> {
    @Query(value = "SELECT * FROM rescat.funding WHERE is_confirmed = ? ORDER BY now() - limit_at LIMIT 4", nativeQuery = true)
    List<Funding> findTop4ByIsConfirmedOrderByFewDaysLeft(Integer isConfirmed);

    @Query(value = "SELECT * FROM rescat.funding WHERE category = ? and is_confirmed = ? ORDER BY now() - limit_at", nativeQuery = true)
    List<Funding> findByCategoryAndIsConfirmedOrderByFewDaysLeft(Integer category, Integer isConfirmed);

    List<Funding> findByWriterAndIsConfirmedOrderByCreatedAtDesc(User writer, Integer isConfirmed);

    List<Funding> findAllByIsConfirmedOrderByCreatedAt(Integer isConfirmed);

    Integer countByIsConfirmed(Integer inConfirmed);

    List<Funding> findByLimitAtBetweenAndCategoryAndIsConfirmed(LocalDateTime start, LocalDateTime end, Integer category, Integer isConfirmed);

    Boolean existsFundingByWriterAndIsConfirmed(User loginUser, Integer isConfirmed);
}
