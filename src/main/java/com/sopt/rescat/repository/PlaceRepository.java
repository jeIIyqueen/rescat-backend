package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Place;
import com.sopt.rescat.domain.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlaceRepository extends CrudRepository<Place, Long> {
    List<Place> findByRegion(Region region);
}
