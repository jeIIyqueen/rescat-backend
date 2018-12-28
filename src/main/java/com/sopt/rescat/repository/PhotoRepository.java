package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Photo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PhotoRepository extends CrudRepository<Photo, Long> {
    Optional<Photo> findByIdx(Long idx);
}
