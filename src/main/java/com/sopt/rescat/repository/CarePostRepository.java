package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import org.springframework.data.repository.CrudRepository;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    Iterable<CarePost> findByType(Integer type);
}
