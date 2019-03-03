package jellyqueen.rescat.service;

import jellyqueen.rescat.domain.CareApplication;
import jellyqueen.rescat.domain.CarePost;
import jellyqueen.rescat.domain.CarePostComment;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.enums.Breed;
import jellyqueen.rescat.domain.enums.RequestStatus;
import jellyqueen.rescat.domain.enums.RequestType;
import jellyqueen.rescat.domain.enums.WarningType;
import jellyqueen.rescat.domain.log.ApprovalLog;
import jellyqueen.rescat.domain.log.WarningLog;
import jellyqueen.rescat.dto.request.CarePostRequestDto;
import jellyqueen.rescat.dto.response.CarePostResponseDto;
import jellyqueen.rescat.exception.*;
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

@Slf4j
@Service
public class CarePostService {

    private CarePostRepository carePostRepository;
    private CarePostCommentRepository carePostCommentRepository;
    private CareApplicationRepository careApplicationRepository;
    private ApprovalLogRepository approvalLogRepository;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private WarningLogRepository warningLogRepository;

    public CarePostService(final CarePostRepository carePostRepository,
                           final CarePostCommentRepository carePostCommentRepository,
                           final CareApplicationRepository careApplicationRepository,
                           final ApprovalLogRepository approvalLogRepository,
                           final NotificationService notificationService,
                           final NotificationRepository notificationRepository,
                           WarningLogRepository warningLogRepository) {
        this.carePostRepository = carePostRepository;
        this.carePostCommentRepository = carePostCommentRepository;
        this.careApplicationRepository = careApplicationRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.warningLogRepository = warningLogRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        if (carePostRepository.existsCarePostByWriterAndIsConfirmed(loginUser, RequestStatus.DEFER.getValue())) {
            throw new AlreadyExistsException("carePost", "게시 승인되지 않은 작성글이 있습니다.");
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
        CarePost carePost = findCarePostBy(idx).setStatus(loginUser);
        return carePost;
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
        CarePost carePost = carePostRepository.findById(carePostIdx)
                .orElseThrow(() -> new NotFoundException("idx", "관련 글을 찾을 수 없습니다."));
        if (!carePost.equalsType(careApplication.getType()))
            throw new InvalidValueException("type", "신청하고자 하는 글의 타입과 명시한 타입이 일치하지 않습니다.");
        if (carePost.getIsFinished())
            throw new InvalidValueException("carePost", "신청이 완료된 글입니다.");
        if (carePost.equalsWriter(loginUser))
            throw new InvalidValueException("user", "작성자는 신청할 수 없습니다.");
        if (carePost.isSubmitted(loginUser))
            throw new AlreadyExistsException("carePostIdx", "이미 신청한 글입니다.");

        CareApplication application = CareApplication.builder().address(careApplication.getAddress()).birth(careApplication.getBirth())
                .carePost(carePost).companionExperience(careApplication.getCompanionExperience())
                .finalWord(careApplication.getFinalWord()).houseType(careApplication.getHouseType())
                .job(careApplication.getJob()).name(careApplication.getName()).phone(careApplication.getPhone())
                .writer(loginUser).type(careApplication.getType()).isAccepted(false).build();

        careApplicationRepository.save(application);

        notificationService.send(application, carePost.getWriter());
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

        notificationService.send(careApplication, careApplication.getWriter());
    }

    public CareApplication getCareApplication(Long careApplicationIdx) {
        CareApplication careApplication = careApplicationRepository.findById(careApplicationIdx)
                .orElseThrow(() -> new NotFoundException("idx", "idx에 해당하는 신청이 존재하지 않습니다."));

        return careApplication;
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

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCarePostRequest(carePost, approver);

        } else if (status.equals(RequestStatus.CONFIRM.getValue())) {
            approveCarePostRequest(carePost, approver);
        }

        notificationService.send(carePost, carePost.getWriter());
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
        CarePostComment comment = carePostCommentRepository.save(carePostComment
                .setWriter(loginUser)
                .setStatus(loginUser)
                .initCarePost(getCarePostBy(carePostIdx)))
                .setWriterNickname()
                .setUserRole();

        User writer = carePostRepository.findById(carePostIdx)
                .orElseThrow(() -> new NotFoundException("carePostIdx", "idx에 해당되는 글이 존재하지 않습니다."))
                .getWriter();


        notificationService.send(carePostComment, writer);
        return comment;
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
    public void warningCarePost(Long idx, User user) {
        CarePost carePost = getCarePostBy(idx);
        carePost.warningCount();

        if (carePost.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 글은 신고할 수 없습니다.");

        if (warningLogRepository.existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(idx, WarningType.CAREPOST, user))
            throw new AlreadyExistsException("idx", "이미 신고한 글은 다시 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(idx)
                .warningType(WarningType.CAREPOST)
                .warningUser(user)
                .build());
    }

    @Transactional
    public void warningCarePostComment(Long commentIdx, User user) {
        CarePostComment carePostComment = getCommentBy(commentIdx);
        carePostComment.warningCount();

        if (carePostComment.getWriter().getIdx().equals(user.getIdx()))
            throw new UnAuthenticationException("idx", "자신이 작성한 댓글은 신고할 수 없습니다.");

        if (warningLogRepository.existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(commentIdx, WarningType.CAREPOSTCOMMENT, user))
            throw new AlreadyExistsException("idx", "이미 신고한 댓글은 다시 신고할 수 없습니다.");

        warningLogRepository.save(WarningLog.builder()
                .warningIdx(commentIdx)
                .warningType(WarningType.CAREPOSTCOMMENT)
                .warningUser(user)
                .build());
    }

}

