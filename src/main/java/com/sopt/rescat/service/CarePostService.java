package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.domain.enums.WarningType;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarePostService {

    private CarePostRepository carePostRepository;
    private CarePostCommentRepository carePostCommentRepository;
    private CareApplicationRepository careApplicationRepository;
    private ApprovalLogRepository approvalLogRepository;
    private NotificationRepository notificationRepository;
    private UserNotificationLogRepository userNotificationLogRepository;
    private WarningLogRepository warningLogRepository;

    public CarePostService(final CarePostRepository carePostRepository,
                           final CarePostCommentRepository carePostCommentRepository,
                           final CareApplicationRepository careApplicationRepository,
                           final ApprovalLogRepository approvalLogRepository,
                           final NotificationRepository notificationRepository,
                           final UserNotificationLogRepository userNotificationLogRepository,
                           WarningLogRepository warningLogRepository) {
        this.carePostRepository = carePostRepository;
        this.carePostCommentRepository = carePostCommentRepository;
        this.careApplicationRepository = careApplicationRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.warningLogRepository = warningLogRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        if (carePostRepository.existsCarePostByWriterAndIsFinished(loginUser, false)) {
            throw new AlreadyExistsException("carePost", "완료되지 않은 작성글이 있습니다.");
        }

        CarePost carePost = carePostRepository.save(carePostRequestDto.toCarePost(false)
                .setWriter(loginUser));
        carePost.initPhotos(carePostRequestDto.convertPhotoUrlsToCarePostPhoto(carePost));
    }

    // viewCount 올리는 경우
    private CarePost findCarePostBy(Long idx) {
        CarePost carePost = getCarePostBy(idx);
        carePostRepository.save(carePost.addViewCount());

        return carePost.setWriterNickname();
    }

    public CarePost findCarePostBy(Long idx, User loginUser) {
        return findCarePostBy(idx).setStatus(loginUser);
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByIsConfirmedOrderByUpdatedAtDesc(RequestStatus.CONFIRM.getValue());
    }

    public Iterable<CarePostResponseDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeAndIsConfirmedOrderByUpdatedAtDesc(type, RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePostResponseDto> find5Post() {
        return carePostRepository.findTop5ByIsConfirmedOrderByUpdatedAtDesc(RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public List<CarePostComment> findCommentsBy(Long idx, User loginUser) {
        return carePostCommentRepository.findByCarePostIdxOrderByCreatedAtAsc(idx).stream()
                .peek((carePostComment) -> {
                    carePostComment.setUserRole();
                    carePostComment.setWriterNickname();
                    carePostComment.setStatus(loginUser);
                }).collect(Collectors.toList());
    }

    public List<Map> getBreeds() {
        return Arrays.stream(Breed.values())
                .map((breedEnum) -> {
                    Map<String, Object> breed = new HashMap<>();
                    breed.put("english", breedEnum.name());
                    breed.put("korean", breedEnum.getValue());
                    return breed;
                })
                .collect(Collectors.toList());
    }


    public Iterable<CarePostResponseDto> findAllBy(User user) {
        return carePostRepository.findByWriterAndIsConfirmedOrderByUpdatedAtDesc(user, RequestStatus.CONFIRM.getValue())
                .stream().map(CarePost::toCarePostDto).collect(Collectors.toList());
    }

    public Integer getCarePostRequestCount() {
        return carePostRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    @Transactional
    public void createCareApplication(CareApplication careApplication, User loginUser, Long carePostIdx) {
        CarePost carePost = carePostRepository.findById(carePostIdx).orElseThrow(() -> new NotFoundException("idx", "관련 글을 찾을 수 없습니다."));
        if (!carePost.equalsType(careApplication.getType()))
            throw new InvalidValueException("type", "신청하고자 하는 글의 타입과 명시한 타입이 일치하지 않습니다.");
        if (carePost.getIsFinished())
            throw new InvalidValueException("carePost", "신청이 완료된 글입니다.");
        if (carePost.equalsWriter(loginUser))
            throw new InvalidValueException("user", "작성자는 신청할 수 없습니다.");
        if (carePost.isSubmitted(loginUser))
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
        if (careApplication.getCarePost().getIsFinished())
            throw new InvalidValueException("carePost", "신청이 완료된 글입니다.");

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
        return carePostRepository.findAllByIsConfirmedOrderByUpdatedAt(RequestStatus.DEFER.getValue())
                .stream()
                .map(CarePost::setWriterNickname)
                .collect(Collectors.toList());
    }

    @Transactional
    public CarePostResponseDto confirmCarePost(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CarePost carePost = getCarePostBy(idx);
        String category = (carePost.getType() == 0) ? "입양" : "임시보호";

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCarePostRequest(carePost, approver);

            Notification notification = Notification.builder()
                    .contents(carePost.getWriter().getNickname() + "님의 " + category + " 등록 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();
            notificationRepository.save(notification);

            userNotificationLogRepository.save(
                    UserNotificationLog.builder()
                            .receivingUser(carePost.getWriter())
                            .notification(notification)
                            .isChecked(RequestStatus.DEFER.getValue())
                            .build());

        } else if (status.equals(RequestStatus.CONFIRM.getValue())) {
            approveCarePostRequest(carePost, approver);

            Notification notification = Notification.builder()
                    .contents(carePost.getWriter().getNickname() + "님의 " + category + " 등록 신청이 승인되었습니다. 좋은 " + category + "자를 만날 수 있기를 응원합니다.")
                    .targetType(RequestType.CAREPOST)
                    .targetIdx(carePost.getIdx())
                    .build();
            notificationRepository.save(notification);

            userNotificationLogRepository.save(
                    UserNotificationLog.builder()
                            .receivingUser(carePost.getWriter())
                            .notification(notification)
                            .isChecked(RequestStatus.DEFER.getValue())
                            .build());
        }

        return carePost.toCarePostDto();
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

    @Transactional
    public void updateCarePostToRecent(Long carePostIdx, User loginUser) {
        CarePost carePost = getCarePostBy(carePostIdx);
        if (!carePost.equalsWriter(loginUser))
            throw new InvalidValueException("idx", "해당 글의 작성자가 아닙니다.");
        carePost.updateUpdatedAt();
    }

    public CarePostComment createComment(Long carePostIdx, CarePostComment carePostComment, User loginUser) {
        return carePostCommentRepository.save(carePostComment
                .setWriter(loginUser)
                .setStatus(loginUser)
                .initCarePost(getCarePostBy(carePostIdx)))
                .setWriterNickname()
                .setUserRole();
    }

    public void deleteComment(Long commentIdx, User loginUser) {
        CarePostComment carePostComment = getCommentBy(commentIdx);
        if (!loginUser.match(carePostComment.getWriter()))
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

    @Transactional
    public void warningCarePost(Long idx, User user){
        CarePost carePost = getCarePostBy(idx);
        carePost.warningCount();

        if(carePost.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 글은 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(idx)
                .warningType(WarningType.CAREPOST)
                .warningUser(user)
                .build());
    }

    @Transactional
    public void warningCarePostComment(Long commentIdx, User user){
        CarePostComment carePostComment = getCommentBy(commentIdx);
        carePostComment.warningCount();

        if(carePostComment.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 댓글은 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(commentIdx)
                .warningType(WarningType.CAREPOSTCOMMENT)
                .warningUser(user)
                .build());
    }

}

