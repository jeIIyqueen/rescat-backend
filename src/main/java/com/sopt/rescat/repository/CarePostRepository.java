package com.sopt.rescat.repository;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeAndIsConfirmedOrderByUpdatedAtDesc(Integer type, Integer isConfirmed);

    Iterable<CarePost> findByIsConfirmedOrderByUpdatedAtDesc(Integer isConfirmed);

    List<CarePost> findTop5ByIsConfirmedOrderByUpdatedAtDesc(Integer isConfirmed);

    List<CarePost> findByWriterAndIsConfirmedOrderByUpdatedAtDesc(User writer, Integer isConfirmed);

    List<CarePost> findAllByIsConfirmedOrderByUpdatedAt(Integer isConfirmed);

    Boolean existsCarePostByWriterAndIsFinished(User writer, Boolean isFinished);
}
