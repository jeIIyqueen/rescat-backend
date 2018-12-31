package com.sopt.rescat.service;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.dto.response.FundingDetailDto;
import com.sopt.rescat.dto.response.FundingDto;
import com.sopt.rescat.exception.NotMatchException;
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

    public FundingDetailDto findByIdx(Long idx) {
        return fundingRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "해당하는 idx가 존재하지 않습니다."))
                .toFundingDetailDto();
    }
}
