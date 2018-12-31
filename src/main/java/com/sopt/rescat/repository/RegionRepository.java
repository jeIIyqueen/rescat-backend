package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends CrudRepository<Region, Long> {
    List<Region> findAll();

    Optional<Region> findByEmdCode(Integer emdCode);

    Optional<Region> findBySdNameAndSggNameAndEmdName(String sdName, String sggName, String emdName);
}
