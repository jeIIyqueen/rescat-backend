package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CareTakerRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CareTakerRequestRepository extends CrudRepository<CareTakerRequest, Long> {
    List<CareTakerRequest> findAllByIsConfirmedOrderByCreatedAt(Integer isConfirmed);
}
