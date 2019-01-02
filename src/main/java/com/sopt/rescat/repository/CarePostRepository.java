package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeAndIsConfirmedAndIsFinishedOrderByCreatedAtDesc(Integer type, Integer isConfirmed, Boolean isFinished);

    Iterable<CarePost> findByIsConfirmedAndOrderByCreatedAtDesc(Integer isConfirmed);

    List<CarePost> findTop5ByIsConfirmedAndIsFinishedOrderByCreatedAtDesc(Integer isConfirmed, Boolean isFinished);
}
