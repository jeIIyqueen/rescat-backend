package com.sopt.rescat.service;

import com.sopt.rescat.domain.Place;
import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.exception.NotFoundException;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MapService {
    private final String DEFAULT_PHOTO_URL = "https://s3.ap-northeast-2.amazonaws.com/rescat/profile.png";

    private final CatRepository catRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final MapRequestRepository mapRequestRepository;
    private final RegionRepository regionRepository;
    private final S3FileService s3FileService;

    public MapService(CatRepository catRepository,
                      PlaceRepository placeRepository,
                      UserRepository userRepository,
                      MapRequestRepository mapRequestRepository,
                      RegionRepository regionRepository,
                      S3FileService s3FileService) {
        this.catRepository = catRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        this.mapRequestRepository = mapRequestRepository;
        this.regionRepository = regionRepository;
        this.s3FileService = s3FileService;
    }

    public User getUser(final Long userIdx){
        User user = userRepository.findByIdx(userIdx);
        if(!(user.getRole() == Role.CARETAKER)){
            throw new UnAuthenticationException("user", "케어테이커 인증을 받지 않은 사용자입니다.");
        }
        return user;
    }

    public MarkerListDto getMarkerListByRegion(final User user, final Optional<Integer> emdCode) {

        Region selectedRegion = user.getMainRegion();
        if(emdCode.isPresent()) {
            if(!getRegionList(user).stream().anyMatch(regionDto -> regionDto.getCode()==emdCode.get())){
                throw new UnAuthenticationException("emdCode", "인가되지 않은 지역입니다.");
            }
            selectedRegion = regionRepository.findByEmdCode(emdCode.get()).orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        }

        List<CatDto> cats = catRepository.findByRegion(selectedRegion).stream().map(cat -> cat.toCatDto()).collect(Collectors.toList());
        List<Place> places = placeRepository.findByRegion(selectedRegion);

        List<PlaceDto> hospitals = new ArrayList<>();
        List<PlaceDto> feeders = new ArrayList<>();

        places.forEach(place -> {
            boolean placeType = (place.getCategory() != 0);
            if (placeType) {
                hospitals.add(place.toPlaceDto());
            } else {
                feeders.add(place.toPlaceDto());
            }
        });

        return MarkerListDto.builder()
                .cats(cats).hospitals(hospitals).feeders(feeders).build();
    }

    @Transactional
    public void saveMarkerRequest(final User user, final MapRequestDto mapRequestDto) throws IOException {
        String markerPhotoUrl = DEFAULT_PHOTO_URL;

        if(mapRequestDto.getPhoto()!=null)
            markerPhotoUrl = s3FileService.upload(mapRequestDto.getPhoto());

        mapRequestRepository.save(mapRequestDto.toMapRequest(user, markerPhotoUrl));
    }

    public List<RegionDto> getRegionList(final User user) {

        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());

        return regions.stream().filter(Objects::nonNull)
                .map(region -> region.toRegionDto())
                .collect(Collectors.toList());
    }

//    public List<List<MapRequestDto>> getMapRequestList(final Long userIdx) {
//        // 관리자이면 전체보기
//        User user = userRepository.findByIdx(userIdx);
//        if (!(user.getRole() == Role.ADMIN)) {
//            throw new UnAuthenticationException("user", "관리자 권한이 필요합니다.");
//        }
//
//
//
//    }

}
