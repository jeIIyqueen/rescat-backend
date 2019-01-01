package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Funding;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FundingRepository extends CrudRepository<Funding, Long> {
    @Query(value = "SELECT * FROM rescat.funding WHERE is_confirmed = ? ORDER BY now() - limit_at LIMIT 4", nativeQuery = true)
    List<Funding> findTop4ByIsConfirmedOrderByFewDaysLeft(Integer isConfirmed);

    @Query(value = "SELECT * FROM rescat.funding WHERE category = ? and is_confirmed = ? ORDER BY now() - limit_at", nativeQuery = true)
    List<Funding> findByCategoryAndIsConfirmedOrderByFewDaysLeft(Integer category, Integer isConfirmed);
}
