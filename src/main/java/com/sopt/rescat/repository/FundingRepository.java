package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Funding;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FundingRepository extends CrudRepository<Funding, Long> {
    @Query(value = "SELECT * FROM rescat.funding ORDER BY now() - limit_at LIMIT 4", nativeQuery = true)
    List<Funding> findTop4ByOrderByFewDaysLeft();

    @Query(value = "SELECT * FROM rescat.funding WHERE category = ? ORDER BY now() - limit_at", nativeQuery = true)
    List<Funding> findByCategoryOrderByFewDaysLeft(Integer category);
}
