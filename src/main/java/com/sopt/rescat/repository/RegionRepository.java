package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

//    @Query(value = "select distinct sdCode, sdName from region", nativeQuery = true)
//    List<SdProjection> findAllProjectedBy();
//
////    @Query(value = "select distinct sggCode, sggName from region")
////    List<Region> findAllProjectedBy
////
////    @Query(value = "select distinct emdCode, emdName from region")
////    List<Region> findAllEmd();
    // todo: projection으로 바꿔보기
    List<Region> findAll();

    Optional<Region> findByEmdCode(Integer emdCode);

}