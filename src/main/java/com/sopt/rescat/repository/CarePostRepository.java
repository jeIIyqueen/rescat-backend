package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeAndIsConfirmedOrderByCreatedAtDesc(Integer type, Boolean isConfirmed);

    Iterable<CarePost> findByIsConfirmedOrderByCreatedAtDesc(Boolean isConfirmed);

    List<CarePost> findTop5ByIsConfirmedOrderByCreatedAtDesc(Boolean isConfirmed);
}
