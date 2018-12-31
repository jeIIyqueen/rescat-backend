package com.sopt.rescat.service;

import com.sopt.rescat.domain.AdvertisementBanner;
import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.exception.NotExistException;
import com.sopt.rescat.repository.AdvertisementBannerRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AdvertisementBannerService {
    private AdvertisementBannerRepository advertisementBannerRepository;

    public AdvertisementBannerService(final AdvertisementBannerRepository advertisementBannerRepository) {
        this.advertisementBannerRepository = advertisementBannerRepository;
    }

    public Iterable<BannerDto> gets() {
        return advertisementBannerRepository.findAll().stream()
                .map(AdvertisementBanner::toBannerDto)
                .collect(Collectors.toList());
    }

    public BannerDto getByRandomIdx() {
        return advertisementBannerRepository.findRandomRow()
                .orElseThrow(NotExistException::new)
                .toBannerDto();
    }
}
