package com.sopt.rescat.service;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.Place;
import com.sopt.rescat.dto.MarkerListDto;
import com.sopt.rescat.repository.CatRepository;
import com.sopt.rescat.repository.PlaceRepository;
import com.sopt.rescat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MapService {
    private final CatRepository catRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public MapService(CatRepository catRepository, PlaceRepository placeRepository, UserRepository userRepository) {
        this.catRepository = catRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
    }

//    public MarkerListDto findByRegion(final String region){
//        List<Cat> cats = catRepository.findByRegion(region);
//        List<Place> places = placeRepository.findByRegion(region);
//        List<Place> hospitals = placeRepository.findByRegionAndCategory(region, 1);
//        List<Place> soupKitchens = placeRepository.findByRegionAndCategory(region, 0);

//        return MarkerListDto.builder().cats(cats).hospitals(hospitals).soupKitchens(soupKitchens).build();
//    }

//    public void create(final Long userIdx, final MapRequest mapRequest){
//        mapRequest
//    }
}
