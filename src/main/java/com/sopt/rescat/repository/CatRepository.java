package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CatRepository extends CrudRepository<Cat, Long> {
    List<Cat> findByRegion(Region region);
}
