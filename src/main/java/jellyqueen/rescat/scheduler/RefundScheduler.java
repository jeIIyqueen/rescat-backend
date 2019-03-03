package jellyqueen.rescat.scheduler;

import jellyqueen.rescat.domain.Funding;
import jellyqueen.rescat.domain.log.ProjectFundingLog;
import jellyqueen.rescat.domain.enums.RequestStatus;
import jellyqueen.rescat.repository.FundingRepository;
import jellyqueen.rescat.repository.ProjectFundingLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RefundScheduler {

    private final FundingRepository fundingRepository;
    private final ProjectFundingLogRepository projectFundingLogRepo;

    public RefundScheduler(final FundingRepository fundingRepository, final ProjectFundingLogRepository projectFundingLogRepo) {
        this.fundingRepository = fundingRepository;
        this.projectFundingLogRepo = projectFundingLogRepo;
    }


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void refundFinishedFunding() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0));
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        List<Funding> projectFundingsFinishedYesterday
                = fundingRepository.findByLimitAtBetweenAndCategoryAndIsConfirmed(start, end, 1, RequestStatus.CONFIRM.getValue())
                .stream().filter(Funding::isAvailableRefund).collect(Collectors.toList());

        projectFundingsFinishedYesterday.forEach(funding -> projectFundingLogRepo.findByFunding(funding).forEach(ProjectFundingLog::refund));
    }


}
