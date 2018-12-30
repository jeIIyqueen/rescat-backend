package com.sopt.rescat.service;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.dto.response.FundingDto;
import com.sopt.rescat.repository.FundingRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class FundingService {
    private FundingRepository fundingRepository;

    public FundingService(final FundingRepository fundingRepository) {
        this.fundingRepository = fundingRepository;
    }

    public Iterable<FundingDto> find4Fundings() {
        return fundingRepository.findTop4ByOrderByFewDaysLeft().stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }

    public Iterable<FundingDto> findAllBy(Integer category) {
        return fundingRepository.findByCategoryOrderByFewDaysLeft(category).stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }
}
