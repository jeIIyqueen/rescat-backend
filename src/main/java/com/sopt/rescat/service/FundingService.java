package com.sopt.rescat.service;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.FundingComment;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.request.FundingRequestDto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.FundingRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FundingService {
    private final static Boolean IS_CONFIRMED  = true;
    private final static Boolean NOT_CONFIRMED = false;

    private FundingRepository fundingRepository;

    public FundingService(final FundingRepository fundingRepository) {
        this.fundingRepository = fundingRepository;
    }

    @Transactional
    public void create(FundingRequestDto fundingRequestDto, User loginUser) {
        Funding funding = fundingRepository.save(fundingRequestDto.toFunding()
                .setWriter(loginUser));

        funding.initPhotos(fundingRequestDto.convertPhotoUrlsToPhotos(funding));
        funding.initCertifications(fundingRequestDto.convertCertificationUrlsToCertifications(funding));
    }

    public Iterable<FundingResponseDto> find4Fundings() {
        return fundingRepository.findTop4ByOrderByFewDaysLeft().stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }

    public Iterable<FundingResponseDto> findAllBy(Integer category) {
        return fundingRepository.findByCategoryOrderByFewDaysLeft(category).stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }

    public Funding findByIdx(Long idx) {
        return getFundingBy(idx)
                .setWriterNickname();
    }

    public List<FundingComment> findCommentsBy(Long idx) {
        return getFundingBy(idx)
                .getComments().stream()
                .peek((fundingComment) -> {
                    fundingComment.setUserRole();
                    fundingComment.setWriterNickname();
                }).collect(Collectors.toList());
    }

    @Transactional
    public void payForMileage(Long idx, Long mileage, User loginUser) {
        // TODO funding 글 작성자의 마일리지 업데이트
        loginUser.updateMileage(mileage * (-1));
        getFundingBy(idx).updateCurrentAmount(mileage);
    }

    @Transactional
    public void confirmFunding(Long idx) {
        getFundingBy(idx).updateConfirmStatus(IS_CONFIRMED);
    }

    private Funding getFundingBy(Long idx) {
        return fundingRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }
}
