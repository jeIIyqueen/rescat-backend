package com.sopt.rescat.repository;

import com.sopt.rescat.domain.MapRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MapRequestRepository extends CrudRepository<MapRequest, Long> {
    List<MapRequest> findByIsConfirmedOrderByCreatedAtDesc(Integer isConfirmed);

    List<MapRequest> findAllByOrderByCreatedAtDesc();

    Integer countByIsConfirmed(Integer isConfirmed);
}
