package com.sopt.rescat.repository;

import com.sopt.rescat.domain.FundingBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundingBannerRepository extends JpaRepository<FundingBanner, Long> {
    List<FundingBanner> findTop4ByOrderByCreatedAtDesc();
}
