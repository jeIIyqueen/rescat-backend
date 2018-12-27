package com.sopt.rescat.service;

import com.sopt.rescat.dto.response.BannerDto;
import com.sopt.rescat.repository.FundingBannerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FundingBannerService {
    private FundingBannerRepository fundingBannerRepository;

    public FundingBannerService(final FundingBannerRepository fundingBannerRepository) {
        this.fundingBannerRepository = fundingBannerRepository;
    }

    public List<BannerDto> get4banners() {
        return fundingBannerRepository.findTop4ByOrderByCreatedAtDesc()
                .stream()
                .map((fundingBanner) -> fundingBanner.toBannerDto())
                .collect(Collectors.toList());
    }
}
