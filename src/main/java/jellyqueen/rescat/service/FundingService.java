package jellyqueen.rescat.service;

import jellyqueen.rescat.domain.Funding;
import jellyqueen.rescat.domain.FundingComment;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.enums.Bank;
import jellyqueen.rescat.domain.enums.RequestStatus;
import jellyqueen.rescat.domain.enums.RequestType;
import jellyqueen.rescat.domain.enums.WarningType;
import jellyqueen.rescat.domain.log.ApprovalLog;
import jellyqueen.rescat.domain.log.ProjectFundingLog;
import jellyqueen.rescat.domain.log.WarningLog;
import jellyqueen.rescat.dto.request.FundingRequestDto;
import jellyqueen.rescat.dto.response.FundingResponseDto;
import jellyqueen.rescat.exception.AlreadyExistsException;
import jellyqueen.rescat.exception.NotMatchException;
import jellyqueen.rescat.exception.UnAuthenticationException;
import jellyqueen.rescat.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FundingService {

    private FundingRepository fundingRepository;
    private FundingCommentRepository fundingCommentRepository;
    private ProjectFundingLogRepository projectFundingLogRepository;
    private ApprovalLogRepository approvalLogRepository;
    private NotificationService notificationService;
    private WarningLogRepository warningLogRepository;

    public FundingService(final FundingRepository fundingRepository,
                          final NotificationService notificationService,
                          FundingCommentRepository fundingCommentRepository, final ProjectFundingLogRepository projectFundingLogRepository,
                          final ApprovalLogRepository approvalLogRepository, final WarningLogRepository warningLogRepository, final UserRepository userRepository) {
        this.fundingRepository = fundingRepository;
        this.fundingCommentRepository = fundingCommentRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.notificationService = notificationService;
        this.warningLogRepository = warningLogRepository;
    }

    @Transactional
    public void create(FundingRequestDto fundingRequestDto, User loginUser) {
        if (fundingRepository.existsFundingByWriterAndIsConfirmed(loginUser, RequestStatus.DEFER.getValue())) {
            throw new AlreadyExistsException("carePost", "게시 승인되지 않은 작성글이 있습니다.");
        }
        Funding funding = fundingRepository.save(fundingRequestDto.toFunding()
                .setWriter(loginUser));

        funding.initPhotos(fundingRequestDto.convertPhotoUrlsToPhotos(funding));
        funding.initCertifications(fundingRequestDto.convertCertificationUrlsToCertifications(funding));
    }

    public Iterable<FundingResponseDto> find4Fundings() {
        return fundingRepository.findTop4ByIsConfirmedOrderByFewDaysLeft(RequestStatus.CONFIRM.getValue()).stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }

    public Iterable<FundingResponseDto> findAllBy(Integer category) {
        return fundingRepository.findByCategoryAndIsConfirmedOrderByFewDaysLeft(category, RequestStatus.CONFIRM.getValue()).stream()
                .map(Funding::toFundingDto)
                .collect(Collectors.toList());
    }

    public Iterable<FundingResponseDto> findAllBy(User user) {
        return fundingRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, RequestStatus.CONFIRM.getValue())
                .stream().map(Funding::toFundingDto).collect(Collectors.toList());
    }

    public Funding findBy(Long idx, User loginUser) {
        return getFundingBy(idx).setWriterNickname().setStatus(loginUser);
    }

    public List<FundingComment> findCommentsBy(Long idx, User loginUser) {
        return fundingCommentRepository.findByFundingIdxOrderByCreatedAtAsc(idx).stream()
                .peek((fundingComment) -> {
                    fundingComment.setUserRole();
                    fundingComment.setWriterNickname();
                    fundingComment.setStatus(loginUser);
                }).collect(Collectors.toList());
    }

    public Integer getFundingCount() {
        return fundingRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    @Transactional
    public void payForMileage(Long idx, Long mileage, User loginUser) {
        Funding funding = getFundingBy(idx);

        if (loginUser.match(funding.getWriter()))
            throw new UnAuthenticationException("token", "본인의 펀딩글은 후원할 수 없습니다.");

        loginUser.updateMileage(mileage * (-1));
        projectFundingLogRepository.save(ProjectFundingLog.builder()
                .amount(mileage)
                .funding(funding)
                .sponsor(loginUser)
                .status(0)
                .build());
        funding.updateCurrentAmount(mileage);
    }

    public Iterable<Funding> getFundingRequests() {
        return fundingRepository
                .findAllByIsConfirmedOrderByCreatedAt(RequestStatus.DEFER.getValue()).stream()
                .map(Funding::setWriterNickname)
                .collect(Collectors.toList());
    }

    @Transactional
    public FundingResponseDto confirmFunding(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        Funding funding = getFundingBy(idx);

        User writer = funding.getWriter();

        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseFundingRequest(funding, approver);
        } else if (status.equals(RequestStatus.CONFIRM.getValue())) {
            approveFundingRequest(funding, approver);
        }

        notificationService.send(funding, funding.getWriter());

        return funding.toFundingDto();
    }

    private void refuseFundingRequest(Funding funding, User approver) {
        approvalLogRepository.save(ApprovalLog.builder()
                .requestType(RequestType.FUNDING)
                .requestIdx(funding.getIdx())
                .requestStatus(RequestStatus.REFUSE)
                .build()
                .setApprover(approver));
        funding.updateConfirmStatus(RequestStatus.REFUSE.getValue());
    }

    private void approveFundingRequest(Funding funding, User approver) {
        funding.updateConfirmStatus(RequestStatus.CONFIRM.getValue());
        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(funding.getIdx())
                .requestType(RequestType.FUNDING)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(approver));
    }

    @Transactional
    public FundingComment createComment(Long idx, FundingComment fundingComment, User loginUser) {

        FundingComment comment = fundingCommentRepository.save(fundingComment
                .setWriter(loginUser)
                .setStatus(loginUser)
                .initFunding(getFundingBy(idx)))
                .setWriterNickname()
                .setUserRole();

        notificationService.send(comment, comment.getFunding().getWriter());

        return comment;
    }

    public void deleteComment(Long commentIdx, User loginUser) {
        FundingComment fundingComment = getCommentBy(commentIdx);
        if (!loginUser.match(fundingComment.getWriter()))
            throw new UnAuthenticationException("token", "삭제 권한을 가진 유저가 아닙니다.");

        fundingCommentRepository.delete(fundingComment);
    }

    private Funding getFundingBy(Long idx) {
        return fundingRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }

    private FundingComment getCommentBy(Long idx) {
        return fundingCommentRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 댓글이 존재하지 않습니다."));
    }

    public List<Map> getBankList() {
        return Arrays.stream(Bank.values())
                .map((bankEnum) -> {
                    Map<String, Object> breed = new HashMap<>();
                    breed.put("english", bankEnum.name());
                    breed.put("korean", bankEnum.getValue());
                    return breed;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void warningFunding(Long idx, User user) {
        Funding funding = getFundingBy(idx);
        funding.warningCount();

        if (funding.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 글은 신고할 수 없습니다.");

        if (warningLogRepository.existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(idx, WarningType.FUNDING, user))
            throw new AlreadyExistsException("idx", "이미 신고한 글은 다시 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(idx)
                .warningType(WarningType.FUNDING)
                .warningUser(user)
                .build());
    }

    @Transactional
    public void warningFundingComment(Long commentIdx, User user) {
        FundingComment fundingComment = getCommentBy(commentIdx);
        fundingComment.warningCount();

        if (fundingComment.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 댓글은 신고할 수 없습니다.");

        if (warningLogRepository.existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(commentIdx, WarningType.FUNDINGCOMMENT, user))
            throw new AlreadyExistsException("idx", "이미 신고한 댓글은 다시 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(commentIdx)
                .warningType(WarningType.FUNDINGCOMMENT)
                .warningUser(user)
                .build());
    }

}
