package com.sopt.rescat.service;

import com.sopt.rescat.domain.Cat;
import com.sopt.rescat.domain.MapRequest;
import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.MarkerDto;
import com.sopt.rescat.dto.RegionDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotFoundException;
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
    private final Integer CONFIRM = 1;
    private final Integer DEFER = 0;
    private final Integer REFUSE = 2;

    private final CatRepository catRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final MapRequestRepository mapRequestRepository;
    private final RegionRepository regionRepository;

    public MapService(CatRepository catRepository,
                      PlaceRepository placeRepository,
                      UserRepository userRepository,
                      MapRequestRepository mapRequestRepository,
                      RegionRepository regionRepository) {
        this.catRepository = catRepository;
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
        this.mapRequestRepository = mapRequestRepository;
        this.regionRepository = regionRepository;
    }

    public List<MarkerDto> getMarkerListByRegion(final User user, final Optional<Integer> emdCode) {
        Region selectedRegion = user.getMainRegion();
        if (emdCode.isPresent()) {
            if (user.isAuthenticatedRegion(emdCode.get())) {
                selectedRegion = regionRepository.findByEmdCode(emdCode.get()).orElseThrow(() -> new InvalidValueException("emdCode", "지역을 찾을 수 없습니다."));
            }
        }
        List<MarkerDto> markerList = new ArrayList<>();
        markerList.addAll(catRepository.findByRegion(selectedRegion).stream()
                .map(Cat::toMarkerDto)
                .collect(Collectors.toList()));
        markerList.addAll(placeRepository.findByRegion(selectedRegion).stream()
                .map(cat -> cat.toMarkerDto())
                .collect(Collectors.toList()));
        return markerList;
    }

    @Transactional
    public void saveMarkerRequest(final User user, final MapRequest mapRequest) throws IOException {
        if(mapRequest.isEditCategory() || mapRequest.hasMarkerIdx()){
            if(!(mapRequest.isEditCategory() && mapRequest.hasMarkerIdx()))
                throw new InvalidValueException("category or markerIdx","값이 입력되지 않았습니다.");
        }

        String[] fullName = mapRequest.getRegionFullName().split(" ");
        Region region = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        mapRequestRepository.save(MapRequest.builder().age(mapRequest.getAge()).etc(mapRequest.getEtc())
                .isConfirmed(DEFER).lat(mapRequest.getLat()).lng(mapRequest.getLng()).name(mapRequest.getName()).photoUrl(mapRequest.getPhotoUrl()).radius(mapRequest.getRadius())
                .registerType(mapRequest.getRegisterType()).requestType(mapRequest.getRequestType()).sex(mapRequest.getSex()).tnr(mapRequest.getTnr()).region(region)
                .address(mapRequest.getAddress()).writer(user).markerIdx(mapRequest.getMarkerIdx()).build());
    }
}