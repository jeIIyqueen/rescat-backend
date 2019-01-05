package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.MarkerType;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.dto.MarkerDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotFoundException;
import com.sopt.rescat.repository.CatRepository;
import com.sopt.rescat.repository.MapRequestRepository;
import com.sopt.rescat.repository.PlaceRepository;
import com.sopt.rescat.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MapService {

    private final CatRepository catRepository;
    private final PlaceRepository placeRepository;
    private final MapRequestRepository mapRequestRepository;
    private final RegionRepository regionRepository;

    public MapService(CatRepository catRepository,
                      PlaceRepository placeRepository,
                      MapRequestRepository mapRequestRepository,
                      RegionRepository regionRepository) {
        this.catRepository = catRepository;
        this.placeRepository = placeRepository;
        this.mapRequestRepository = mapRequestRepository;
        this.regionRepository = regionRepository;
    }

    public List<MarkerDto> getMarkerListByRegion(final User user, final Optional<Integer> emdCode) {
        Region selectedRegion = user.getMainRegion();
        if (emdCode.isPresent() && user.isAuthenticatedRegion(emdCode.get())) {
            selectedRegion = regionRepository.findByEmdCode(emdCode.get())
                    .orElseThrow(() -> new InvalidValueException("emdCode", "지역을 찾을 수 없습니다."));
        }

        List<MarkerDto> markerList = new ArrayList<>();
        markerList.addAll(catRepository.findByRegion(selectedRegion).stream()
                .map(Cat::toMarkerDto)
                .collect(Collectors.toList()));
        markerList.addAll(placeRepository.findByRegion(selectedRegion).stream()
                .map(Place::toMarkerDto)
                .collect(Collectors.toList()));
        return markerList;
    }

    public Integer getMarkerRequestCount() {
        return mapRequestRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    @Transactional
    public void saveMarkerRequest(final User user, final MapRequest mapRequest) throws IOException {
        if (mapRequest.isEditCategory()) {
            if (!mapRequest.hasMarkerIdx())
                throw new InvalidValueException("category or markerIdx", "값이 입력되지 않았습니다.");
            if (!isAmendable(mapRequest))
                throw new NotFoundException("markerIdx", "존재하지 않는 마커입니다.");
        }

        String[] fullName = mapRequest.getRegionFullName().split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region region = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        user.isAuthenticatedRegion(region.getEmdCode());

        mapRequestRepository.save(MapRequest.builder().age(mapRequest.getAge()).etc(mapRequest.getEtc())
                .isConfirmed(RequestStatus.DEFER.getValue()).lat(mapRequest.getLat()).lng(mapRequest.getLng()).name(mapRequest.getName()).photoUrl(mapRequest.getPhotoUrl()).radius(mapRequest.getRadius())
                .registerType(mapRequest.getRegisterType()).requestType(mapRequest.getRequestType()).sex(mapRequest.getSex()).tnr(mapRequest.getTnr()).region(region)
                .address(mapRequest.getAddress()).writer(user).markerIdx(mapRequest.getMarkerIdx()).phone(mapRequest.getPhone()).build());
    }

    public List<MapRequest> getAllMapRequest() {
        return mapRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(MapRequest::setWriterName).collect(Collectors.toList());
    }

    @Transactional
    public MapRequest approveMapRequest(Long mapRequestIdx) {
        MapRequest mapRequest = mapRequestRepository.findById(mapRequestIdx).orElseThrow(() -> new NotFoundException("mapRequestIdx", "존재하지 않는 등록/수정 요청입니다."));

        if (mapRequest.getRequestType().equals(RequestStatus.DEFER.getValue())) {
            save(mapRequest);
        }
        if (mapRequest.getRequestType().equals(RequestStatus.CONFIRM.getValue())) {
            if (!isAmendable(mapRequest)) {
                throw new NotFoundException("markerIdx", "존재하지 않는 마커입니다.");
            }
            save(mapRequest);
        }
        return mapRequest.setIsConfirmed(RequestStatus.CONFIRM.getValue());
    }

    private void save(MapRequest mapRequest) {
        if (mapRequest.getRegisterType().equals(MarkerType.CAFETERIA.getValue())
                || mapRequest.getRegisterType().equals(MarkerType.HOSPITAL.getValue())) {
            placeRepository.save(mapRequest.toPlace());
            return;
        }

        if (mapRequest.getRegisterType().equals(MarkerType.Cat.getValue())) {
            catRepository.save(mapRequest.toCat());
            return;
        }

        throw new InvalidValueException("registerType", "유효하지 않은 값을 선택하였습니다.");
    }

    private boolean isAmendable(MapRequest mapRequest) {
        if (mapRequest.getRegisterType().equals(MarkerType.CAFETERIA.getValue())
                || mapRequest.getRegisterType().equals(MarkerType.HOSPITAL.getValue()))
            return placeRepository.existsById(mapRequest.getMarkerIdx());

        if (mapRequest.getRegisterType().equals(MarkerType.Cat.getValue()))
            return catRepository.existsById(mapRequest.getMarkerIdx());

        throw new InvalidValueException("registerType", "유효하지 않은 값을 선택하였습니다.");
    }

    public MapRequest refuseMapRequest(Long mapRequestIdx) {
        MapRequest mapRequest = mapRequestRepository.findById(mapRequestIdx)
                .orElseThrow(() -> new NotFoundException("mapRequestIdx", "존재하지 않는 등록/수정 요청입니다."));
        return mapRequest.setIsConfirmed(RequestStatus.REFUSE.getValue());
    }
}
