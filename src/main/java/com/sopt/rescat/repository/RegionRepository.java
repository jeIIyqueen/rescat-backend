package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RegionRepository extends CrudRepository<Region, Long> {
    Optional<Region> findByEmdCode(Integer emdCode);
}
