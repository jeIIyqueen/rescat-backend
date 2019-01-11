package com.sopt.rescat.repository;

import com.sopt.rescat.domain.request.CareTakerRequest;
import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CareTakerRequestRepository extends CrudRepository<CareTakerRequest, Long> {
    List<CareTakerRequest> findAllByIsConfirmedOrderByCreatedAtDesc(Integer isConfirmed);

    Integer countByIsConfirmed(Integer isConfirmed);

    boolean existsCareTakerRequestByWriterAndIsConfirmedAndType(User user, Integer isConfirmed, Integer type);
}
