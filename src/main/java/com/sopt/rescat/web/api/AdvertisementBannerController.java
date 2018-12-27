package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.AdvertisementBanner;
import com.sopt.rescat.service.AdvertisementBannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/banners/advertisement")
public class AdvertisementBannerController {
    private AdvertisementBannerService advertisementBannerService;

    public AdvertisementBannerController(AdvertisementBannerService advertisementBannerService) {
        this.advertisementBannerService = advertisementBannerService;
    }

    @GetMapping("")
    public ResponseEntity<Iterable<AdvertisementBanner>> list() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementBannerService.gets());
    }
}
