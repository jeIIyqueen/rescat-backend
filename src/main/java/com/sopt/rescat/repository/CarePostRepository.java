package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeAndIsConfirmedOrderByCreatedAtDesc(Integer type, Integer isConfirmed);

    Iterable<CarePost> findByIsConfirmedOrderByCreatedAtDesc(Integer isConfirmed);

    List<CarePost> findTop5ByIsConfirmedOrderByCreatedAtDesc(Integer isConfirmed);
}
