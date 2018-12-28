package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Region;
import com.sopt.rescat.dto.RegionDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends CrudRepository<Region, Long> {
    Optional<Region> findByEmdcode(Integer emdcode);

//    @Query("select new RegionDto(sdcode, sdname) from region")
//    List<RegionDto> findAll();
}
