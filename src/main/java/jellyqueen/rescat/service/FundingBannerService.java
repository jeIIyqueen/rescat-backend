package jellyqueen.rescat.service;

import jellyqueen.rescat.domain.FundingBanner;
import jellyqueen.rescat.dto.response.BannerDto;
import jellyqueen.rescat.repository.FundingBannerRepository;
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
                .map(FundingBanner::toBannerDto)
                .collect(Collectors.toList());
    }
}
