package com.sopt.rescat.service;

import com.sopt.rescat.domain.ApprovalLog;
import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.ApprovalLogRepository;
import com.sopt.rescat.repository.CarePostPhotoRepository;
import com.sopt.rescat.repository.CarePostRepository;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarePostService {

    private CarePostRepository carePostRepository;
    private ApprovalLogRepository approvalLogRepository;

    public CarePostService(final CarePostRepository carePostRepository, final ApprovalLogRepository approvalLogRepository) {
        this.carePostRepository = carePostRepository;
        this.approvalLogRepository = approvalLogRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        CarePost carePost = carePostRepository.save(carePostRequestDto.toCarePost()
                .setWriter(loginUser));

        carePost.initPhotos(carePostRequestDto.convertPhotoUrlsToCarePostPhoto(carePost));
    }

    public Iterable<CarePostResponseDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeAndIsConfirmedOrderByCreatedAtDesc(type, RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByIsConfirmedOrderByCreatedAtDesc(RequestStatus.CONFIRM.getValue());
    }

    public Iterable<CarePostResponseDto> find5Post() {
        return carePostRepository.findTop5ByIsConfirmedOrderByCreatedAtDesc(RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public CarePost findCarePostBy(Long idx) {
        return getCarePostBy(idx).setWriterNickname();
    }

    public List<CarePostComment> findCommentsBy(Long idx) {
        return getCarePostBy(idx)
                .getComments().stream()
                .peek((carePostComment) -> {
                    carePostComment.setUserRole();
                    carePostComment.setWriterNickname();
                }).collect(Collectors.toList());
    }

    public Integer getCarePostRequestCount() {
        return carePostRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    private CarePost getCarePostBy(Long idx) {
        return carePostRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }

    public List<Breed> getBreeds() {
        return Arrays.asList(Breed.values());
    }


    public Iterable<CarePost> findAllByUser(User user) {
        return carePostRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, RequestStatus.CONFIRM.getValue());
    }

    public Iterable<CarePost> getCarePostRequests(){
        return new ArrayList<>(carePostRepository.findAllByIsConfirmedOrderByCreatedAt(RequestStatus.DEFER.getValue()));
    }

    @Transactional
    public void confirmCarePost(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CarePost carePost = getCarePostBy(idx);

        // 거절일 경우
        if(status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCarePostRequest(carePost, approver);
            return;
        }

        // 승인일 경우
        approveCarePostRequest(carePost, approver);
    }

    private void refuseCarePostRequest(CarePost carePost, User approver) {
        approvalLogRepository.save(ApprovalLog.builder()
                .requestType(RequestType.CAREPOST)
                .requestIdx(carePost.getIdx())
                .requestStatus(RequestStatus.REFUSE)
                .build()
                .setApprover(approver));
        carePost.updateConfirmStatus(RequestStatus.REFUSE.getValue());
    }

    private void approveCarePostRequest(CarePost carePost, User approver) {
        carePost.updateConfirmStatus(RequestStatus.CONFIRM.getValue());
        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(carePost.getIdx())
                .requestType(RequestType.CAREPOST)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(approver));
    }
}
