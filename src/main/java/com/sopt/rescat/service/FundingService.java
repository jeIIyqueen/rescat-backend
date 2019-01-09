package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.Bank;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.domain.enums.WarningType;
import com.sopt.rescat.dto.request.FundingRequestDto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FundingService {

    private FundingRepository fundingRepository;
    private FundingCommentRepository fundingCommentRepository;
    private ProjectFundingLogRepository projectFundingLogRepository;
    private ApprovalLogRepository approvalLogRepository;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private WarningLogRepository warningLogRepository;

    public FundingService(final FundingRepository fundingRepository,
                          final NotificationService notificationService,
                          final NotificationRepository notificationRepository,
                          FundingCommentRepository fundingCommentRepository, final ProjectFundingLogRepository projectFundingLogRepository,
                          final ApprovalLogRepository approvalLogRepository, final WarningLogRepository warningLogRepository) {
        this.fundingRepository = fundingRepository;
        this.fundingCommentRepository = fundingCommentRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.warningLogRepository = warningLogRepository;
    }

    @Transactional
    public void create(FundingRequestDto fundingRequestDto, User loginUser) {

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

        if(loginUser.match(funding.getWriter()))
            throw new UnAuthenticationException("token", "본인의 펀딩글은 후원할 수 없습니다.");

        loginUser.updateMileage(mileage * (-1));
        projectFundingLogRepository.save(ProjectFundingLog.builder()
                .amount(mileage)
                .funding(funding)
                .sponsor(loginUser)
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

        if (status.equals(RequestStatus.REFUSE.getValue())){
            refuseFundingRequest(funding, approver);

            Notification notification = new Notification().builder()
                    .contents(writer.getNickname() + "님의 후원글 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();

            notificationRepository.save(notification);
            notificationService.createNotification(writer, notification);
        }
        else if (status.equals(RequestStatus.CONFIRM.getValue())) {
            approveFundingRequest(funding, approver);

            Notification notification = new Notification().builder()
                    .contents(writer.getNickname() + "님의 후원글 신청이 승인되었습니다. 회원님의 목표금액 달성을 응원합니다.")
                    .targetType(RequestType.FUNDING)
                    .targetIdx(funding.getIdx())
                    .build();

            notificationRepository.save(notification);
            notificationService.createNotification(writer, notification);
        }

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
        return fundingCommentRepository.save(fundingComment
                .setWriter(loginUser)
                .initFunding(getFundingBy(idx)))
                .setWriterNickname()
                .setUserRole();
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
    public void warningFunding(Long idx, User user){
        Funding funding = getFundingBy(idx);
        funding.warningCount();

        if(funding.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 글은 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(idx)
                .warningType(WarningType.FUNDING)
                .warningUser(user)
                .build());
    }

    @Transactional
    public void warningFundingComment(Long commentIdx, User user){
        FundingComment fundingComment = getCommentBy(commentIdx);
        fundingComment.warningCount();

        if(fundingComment.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 댓글은 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(commentIdx)
                .warningType(WarningType.FUNDINGCOMMENT)
                .warningUser(user)
                .build());
    }
}
