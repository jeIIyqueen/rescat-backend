package com.sopt.rescat.service;

import com.sopt.rescat.domain.AdvertisementBanner;
import com.sopt.rescat.repository.AdvertisementBannerRepository;
import org.springframework.stereotype.Service;

@Service
public class AdvertisementBannerService {
    private AdvertisementBannerRepository advertisementBannerRepository;

    public AdvertisementBannerService(AdvertisementBannerRepository advertisementBannerRepository) {
        this.advertisementBannerRepository = advertisementBannerRepository;
    }

    public Iterable<AdvertisementBanner> gets() {
        return advertisementBannerRepository.findAll();
    }
}
