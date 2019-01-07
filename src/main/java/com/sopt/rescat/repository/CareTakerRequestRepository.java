package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CareTakerRequestRepository extends CrudRepository<CareTakerRequest, Long> {
    List<CareTakerRequest> findAllByIsConfirmedOrderByCreatedAt(Integer isConfirmed);

    Integer countByIsConfirmed(Integer isConfirmed);

    boolean existsCareTakerRequestByWriterAndIsConfirmed(User user, Integer isConfirmed);
}
