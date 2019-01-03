package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.request.FundingRequestDto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import com.sopt.rescat.repository.ApprovalLogRepository;
import com.sopt.rescat.repository.FundingCommentRepository;
import com.sopt.rescat.repository.FundingRepository;
import com.sopt.rescat.repository.ProjectFundingLogRepository;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FundingService {

    private FundingRepository fundingRepository;
    private FundingCommentRepository fundingCommentRepository;
    private ProjectFundingLogRepository projectFundingLogRepository;
    private ApprovalLogRepository approvalLogRepository;

    public FundingService(final FundingRepository fundingRepository,
                          FundingCommentRepository fundingCommentRepository, final ProjectFundingLogRepository projectFundingLogRepository,
                          final ApprovalLogRepository approvalLogRepository) {
        this.fundingRepository = fundingRepository;
        this.fundingCommentRepository = fundingCommentRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
        this.approvalLogRepository = approvalLogRepository;
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

    public Funding findByIdx(Long idx) {
        return getFundingBy(idx).setWriterNickname();
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
        Funding funding = getFundingBy(idx);

        loginUser.updateMileage(mileage * (-1));
        projectFundingLogRepository.save(ProjectFundingLog.builder()
                .amount(mileage)
                .funding(funding)
                .sponsor(loginUser)
                .build());
        funding.updateCurrentAmount(mileage);
    }


    public Iterable<Funding> getFundingRequests() {
        return new ArrayList<>(fundingRepository
                .findAllByIsConfirmedOrderByCreatedAt(RequestStatus.DEFER.getValue()));
    }

    @Transactional
    public void confirmFunding(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        Funding funding = getFundingBy(idx);

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseFundingRequest(funding, approver);
            return;
        }

        // 승인일 경우
        approveFundingRequest(funding, approver);
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
        if(!loginUser.match(fundingComment.getWriter()))
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

    public Iterable<Funding> findAllByUser(User user) {
        return fundingRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, RequestStatus.CONFIRM.getValue());
    }
}
