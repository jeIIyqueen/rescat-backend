package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Region;
import com.sopt.rescat.dto.RegionDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends CrudRepository<Region, Long> {
    Optional<Region> findByemdCode(Integer emdCode);

    @Query("select distinct new RegionDto(sdCode, sdName) from region")
    List<RegionDto> findAllSd();

    @Query("select distinct new RegionDto(sggCode, sggName) from region")
    List<RegionDto> findAllSgg();

    @Query("select distinct new RegionDto(emdCode, emdName) from region")
    List<RegionDto> findAllEmd();
}
