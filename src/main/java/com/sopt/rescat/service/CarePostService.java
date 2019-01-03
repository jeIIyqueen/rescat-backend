package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.ApprovalLogRepository;
import com.sopt.rescat.repository.CareApplicationRepository;
import com.sopt.rescat.repository.CarePostCommentRepository;
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
    private CarePostCommentRepository carePostCommentRepository;
    private CareApplicationRepository careApplicationRepository;
    private ApprovalLogRepository approvalLogRepository;


    public CarePostService(final CarePostRepository carePostRepository,
                           final CarePostCommentRepository carePostCommentRepository,
                           final CareApplicationRepository careApplicationRepository,
                           final ApprovalLogRepository approvalLogRepository) {
        this.carePostRepository = carePostRepository;
        this.carePostCommentRepository = carePostCommentRepository;
        this.careApplicationRepository = careApplicationRepository;
        this.approvalLogRepository = approvalLogRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        CarePost carePost = carePostRepository.save(carePostRequestDto.toCarePost(false)
                .setWriter(loginUser));
        carePost.initPhotos(carePostRequestDto.convertPhotoUrlsToCarePostPhoto(carePost));
    }

    public CarePost findBy(Long idx) {
        return getCarePostBy(idx)
                .setWriterNickname()
                .addViewCount();
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

    public CarePost findCarePostBy(Long idx, User loginUser) {
        if(loginUser != null)
            return getCarePostBy(idx).setWriterNickname().setSubmitStatus(loginUser);
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

    public List<Breed> getBreeds() {
        return Arrays.asList(Breed.values());
    }


    public Iterable<CarePost> findAllByUser(User user) {
        return carePostRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, RequestStatus.CONFIRM.getValue());
    }

    @Transactional
    public void createCareApplication(CareApplication careApplication, User loginUser, Long carePostIdx) {
        CarePost carePost = carePostRepository.findById(carePostIdx).orElseThrow(() -> new NotFoundException("idx", "관련 글을 찾을 수 없습니다."));
        if(!carePost.equalsType(careApplication.getType()))
            throw new InvalidValueException("type", "신청하고자 하는 글의 타입과 명시한 타입이 일치하지 않습니다.");
        if(carePost.isFinished())
            throw new InvalidValueException("carePost", "신청이 완료된 글입니다.");

        if(carePost.equalsWriter(loginUser))
            throw new InvalidValueException("user", "작성자는 신청할 수 없습니다.");

        if(carePost.isSubmitted(loginUser))
            throw new AlreadyExistsException("carePostIdx", "이미 신청한 글입니다.");

        careApplicationRepository.save(
                CareApplication.builder().address(careApplication.getAddress()).birth(careApplication.getBirth())
                        .carePost(carePost).companionExperience(careApplication.getCompanionExperience())
                        .finalWord(careApplication.getFinalWord()).houseType(careApplication.getHouseType())
                        .job(careApplication.getJob()).name(careApplication.getName()).phone(careApplication.getPhone())
                        .writer(loginUser).type(careApplication.getType()).isAccepted(false).build());
    }

    @Transactional
    public void acceptCareApplication(Long careApplicationIdx, User loginUser) {
        CareApplication careApplication = careApplicationRepository.findById(careApplicationIdx).orElseThrow(() -> new NotFoundException("idx", "신청서를 찾을 수 없습니다."));
        careApplication.getCarePost().isFinished();

        careApplication.accept(loginUser);
        careApplication.getCarePost().finish();

        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(careApplication.getIdx())
                .requestType(RequestType.CAREAPPLICATION)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(loginUser));
    }

    public Iterable<CarePost> getCarePostRequests() {
        return new ArrayList<>(carePostRepository.findAllByIsConfirmedOrderByCreatedAt(RequestStatus.DEFER.getValue()));
    }

    @Transactional
    public void confirmCarePost(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CarePost carePost = getCarePostBy(idx);

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
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

    public CarePostComment createComment(Long carePostIdx, CarePostComment carePostComment, User loginUser) {
        return carePostCommentRepository.save(carePostComment
                .setWriter(loginUser)
                .initCarePost(getCarePostBy(carePostIdx)))
                .setWriterNickname()
                .setUserRole();
    }

    public void deleteComment(Long commentIdx, User loginUser) {
        CarePostComment carePostComment = getCommentBy(commentIdx);
        if(!loginUser.match(carePostComment.getWriter()))
            throw new UnAuthenticationException("token", "삭제 권한을 가진 유저가 아닙니다.");

        carePostCommentRepository.delete(carePostComment);
    }

    private CarePostComment getCommentBy(Long commentIdx) {
        return carePostCommentRepository.findById(commentIdx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 댓글이 존재하지 않습니다."));
    }

    private CarePost getCarePostBy(Long idx) {
        return carePostRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }
}

