package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeOrderByCreatedAtDesc(Integer type);

    Iterable<CarePost> findByOrderByCreatedAtDesc();

    List<CarePost> findTop5ByOrderByCreatedAtDesc();
}
