package com.sopt.rescat.repository;

import com.sopt.rescat.domain.AdvertisementBanner;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdvertisementBannerRepository extends CrudRepository<AdvertisementBanner, Long> {
    long count();

    @Query(value = "SELECT * FROM rescat.advertisement_banner ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<AdvertisementBanner> findRandomRow();
}
