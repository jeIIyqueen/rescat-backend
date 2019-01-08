package com.sopt.rescat.service;


import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.*;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiClass;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiResult;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sun.security.provider.certpath.OCSPResponse;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final String ID_REGEX = "^[a-z]+[a-z0-9]{5,19}$";
    private final String NICKNAME_REGEX = "^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{2,20}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectFundingLogRepository projectFundingLogRepository;
    private final CareTakerRequestRepository careTakerRequestRepository;
    private final RegionRepository regionRepository;
    private final ApprovalLogRepository approvalLogRepository;
    private final UserNotificationLogRepository userNotificationLogRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;


    @Value("${GABIA.SMSPHONENUMBER}")
    private String ADMIN_PHONE_NUMBER;
    @Value("${GABIA.SMSID}")
    private String smsId;
    @Value("${GABIA.APIKEY}")
    private String apiKey;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
                       final CareTakerRequestRepository careTakerRequestRepository, final ProjectFundingLogRepository projectFundingLogRepository,
                       final RegionRepository regionRepository, final ApprovalLogRepository approvalLogRepository,
                       final UserNotificationLogRepository userNotificationLogRepository, final NotificationRepository notificationRepository,
                       final NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.regionRepository = regionRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;

    }

    public User getUserBy(Long userIdx) {
        return userRepository.findByIdx(userIdx);
    }

    public Boolean isExistingId(String id) {
        if (!id.matches(ID_REGEX))
            throw new InvalidValueException("id", "아이디는 영문자로 시작하는 6~20자 영문자 또는 숫자이어야 합니다.");

        if (userRepository.findById(id).isPresent()) {
            throw new AlreadyExistsException("id", "이미 사용중인 ID 입니다.");
        }
        return Boolean.FALSE;
    }

    public Boolean isExistingNickname(String nickname) {
        if (!nickname.matches(NICKNAME_REGEX))
            throw new InvalidValueException("nickname", "닉네임은 특수문자 제외 2~20자이어야 합니다.");

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new AlreadyExistsException("nickname", "이미 사용중인 Nickname 입니다.");
        }
        return Boolean.FALSE;
    }

    @Transactional
    public User create(UserJoinDto userJoinDto) {
        isExistingId(userJoinDto.getId());
        isExistingNickname(userJoinDto.getNickname());
        return userRepository.save(userJoinDto.toUser(passwordEncoder.encode(userJoinDto.getPassword())));
    }

    @Transactional
    public User login(UserLoginDto userLoginDto) {
        User savedUser = userRepository.findById(userLoginDto.getId())
                .orElseThrow(() -> new UnAuthenticationException("id", "해당 ID를 가진 사용자가 존재하지 않습니다."));
        savedUser.matchPasswordBy(userLoginDto, passwordEncoder);
        savedUser.updateDeviceToken(userLoginDto.getDeviceToken());

        return savedUser;
    }

    public AuthenticationCodeVO sendSms(String phone) {
        int randomCode = getRandomCode();
        String arr[] = {
                "sms",
                "rescat",
                "rescat 입니다.",                                   // 제목
                "rescat에서 보낸 인증번호 [" + randomCode + "] 입니다.", // 본문
                ADMIN_PHONE_NUMBER,                               // 발신번호
                phone,                                            // 수신번호
                "0"                                               // 즉시발송
        };

        ApiClass api = new ApiClass(this.smsId, this.apiKey);
        ApiResult res = api.getResult(api.send(arr));
        if (res.getCode().equals("0000")) {
            return new AuthenticationCodeVO(randomCode);
        }
        log.debug("sendSms: ", res.getCode() + "", res.getMesg());
        throw new FailureException("문자 발송을 실패했습니다.");
    }

    public UserMypageDto getUserMypage(User user) {
        List<RegionDto> regions = getRegionList(user);
        return UserMypageDto.builder()
                .regions(regions)
                .id(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .regions(regions)
                .isFinished(!careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmed(user,RequestStatus.DEFER.getValue()))
                .build();
    }

    public Integer getCareTakerRequestCount() {
        return careTakerRequestRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    @Transactional
    public void saveCareTakerRequest(final User user, CareTakerRequest careTakerRequest) {

        if (careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmed(user, RequestStatus.DEFER.getValue()))
            throw new AlreadyExistsException("careTakerRequest", "아직 완료되지 않은 케어테이커 신청이 있습니다.");
        if(careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmed(user,RequestStatus.CONFIRM.getValue()))
            throw new AlreadyExistsException("careTakerRequest", "이미 케어테이커 인증이 완료됐습니다.");

        String[] fullName = careTakerRequest.getRegionFullName().split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region region = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));


        careTakerRequestRepository.save(CareTakerRequest.builder()
                .authenticationPhotoUrl(careTakerRequest.getAuthenticationPhotoUrl())
                .isConfirmed(RequestStatus.DEFER.getValue())
                .mainRegion(region)
                .name(careTakerRequest.getName())
                .phone(careTakerRequest.getPhone())
                .writer(user)
                .type(careTakerRequest.getType())
                .build());
    }

    public List<RegionDto> getRegionList(final User user) {

        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());

        return regions.stream().filter(Objects::nonNull)
                .map(Region::toRegionDto)
                .collect(Collectors.toList());
    }

    public Iterable<CareTakerRequest> getCareTakerRequest() {
        return careTakerRequestRepository.findAllByIsConfirmedOrderByCreatedAt(RequestStatus.DEFER.getValue())
                .stream().peek(CareTakerRequest::fillUserNickname)
                .collect(Collectors.toList());
    }

    public List<Funding> getSupportingFundings(User user) {
        List<ProjectFundingLog> projectFundingLogs = projectFundingLogRepository.findBySponsorOrderByCreatedAtDesc(user);
        return getFundingsByLogs(projectFundingLogs);
    }

    private List<Funding> getFundingsByLogs(List<ProjectFundingLog> projectFundingLogs) {
        return projectFundingLogs.stream()
                .map(ProjectFundingLog::getFunding)
                .distinct()
                .collect(Collectors.toList());
    }

    public UserMypageDto getEditUser(User user) {
        return UserMypageDto.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void editUserNickname(User user, String nickname) {
        if (!isExistingNickname(nickname))
            user.updateNickname(nickname);
    }

    @Transactional
    public void editUserPhone(User user, String phone) {
            user.updatePhone(phone);
    }

    @Transactional
    public void editUserPassword(User user, UserPasswordDto userPasswordDto) {

        if (!passwordEncoder.matches(userPasswordDto.getPassword(), user.getPassword()))
            throw new NotMatchException("password", "비밀번호가 틀렸습니다.");

        if (userPasswordDto.getPassword().equals(userPasswordDto.getNewPassword()))
            throw new AlreadyExistsException("newPassword", "현재 사용중인 PASSWORD입니다.");
        if (userPasswordDto.checkValidPassword())
            user.updatePassword(passwordEncoder.encode(userPasswordDto.getNewPassword()));
    }

    @Transactional
    public void approveCareTaker(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CareTakerRequest careTakerRequest = careTakerRequestRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 요청이 존재하지 않습니다."));

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCareTakerRequest(careTakerRequest, approver);

            Notification notification = Notification.builder()
                    .contents(careTakerRequest.getWriter().getNickname() + "님의 케어테이커 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();
            notificationRepository.save(notification);

            userNotificationLogRepository.save(
                    UserNotificationLog.builder()
                            .receivingUser(careTakerRequest.getWriter())
                            .notification(notification)
                            .isChecked(RequestStatus.DEFER.getValue())
                            .build());
            return;
        }

        // 승인일 경우
        approveCareTakerRequest(careTakerRequest, approver);

        Notification notification = Notification.builder()
                .contents(careTakerRequest.getWriter().getNickname() + "님의 케어테이커 신청이 승인되었습니다. 앞으로 활발한 활동 부탁드립니다.")
                .build();
        notificationRepository.save(notification);

        userNotificationLogRepository.save(UserNotificationLog.builder()
                .receivingUser(careTakerRequest.getWriter())
                .notification(notification)
                .isChecked(RequestStatus.DEFER.getValue())
                .build());

    }

    private void refuseCareTakerRequest(CareTakerRequest careTakerRequest, User approver) {
        approvalLogRepository.save(ApprovalLog.builder()
                .requestType(RequestType.CARETAKER)
                .requestIdx(careTakerRequest.getIdx())
                .requestStatus(RequestStatus.REFUSE)
                .build()
                .setApprover(approver));
        careTakerRequest.refuse();
    }

    private void approveCareTakerRequest(CareTakerRequest careTakerRequest, User approver) {
        careTakerRequest.approve();
        careTakerRequest.getWriter().grantCareTakerAuth(careTakerRequest.getPhone(), careTakerRequest.getName(), careTakerRequest.getMainRegion());
        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(careTakerRequest.getIdx())
                .requestType(RequestType.CARETAKER)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(approver));
    }

    @Transactional
    public void deleteRegion(User user, Integer emdCode) {

        Region deleteRegion = regionRepository.findByEmdCode(emdCode)
                .orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        if (deleteRegion.equals(user.getMainRegion()))
            user.deleteMainRegion(user.getSubRegion1(), user.getSubRegion2());

        else if (deleteRegion.equals(user.getSubRegion1()))
            user.deleteSubRegion1(user.getSubRegion2());

        else if (deleteRegion.equals(user.getSubRegion2()))
            user.deleteSubRegion2();

        else
            throw new NotMatchException("mainRegion or subRegion1 or subRegion2", "유저에게 존재하지 않는 지역코드입니다.");
    }

    @Transactional
    public void saveAddRegionRequest(final User user, Integer emdCode, String authenticationPhotoUrl, Integer type) {

        Region region = regionRepository.findByEmdCode(emdCode)
                .orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());

        if(regions.contains(region))
            throw new AlreadyExistsException("emdCode", "이미 존재하는 지역입니다.");

        if (user.getSubRegion1() == null) {
            careTakerRequestRepository.save(CareTakerRequest.builder()
                    .authenticationPhotoUrl(authenticationPhotoUrl)
                    .isConfirmed(RequestStatus.DEFER.getValue())
                    .mainRegion(user.getMainRegion())
                    .subRegion1(region)
                    .name(user.getName())
                    .phone(user.getPhone())
                    .writer(user)
                    .type(type)
                    .build());
        } else if (user.getSubRegion1() != null) {
            careTakerRequestRepository.save(CareTakerRequest.builder()
                    .authenticationPhotoUrl(authenticationPhotoUrl)
                    .isConfirmed(RequestStatus.DEFER.getValue())
                    .mainRegion(user.getMainRegion())
                    .subRegion2(region)
                    .name(user.getName())
                    .phone(user.getPhone())
                    .writer(user)
                    .type(type)
                    .build());
        }
    }

    public void editUserRegion(User user, List<Region> receivedRegions){
        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());

        if(regions.equals(receivedRegions)){
            user.updateRegions(receivedRegions);
        }
    }

    @Transactional
    public void confirmedAddRegion(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CareTakerRequest careTakerRequest = careTakerRequestRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 요청이 존재하지 않습니다."));

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseAddRegionRequest(careTakerRequest, approver);
            return;
        }

        // 승인일 경우
        approveAddRegionRequest(careTakerRequest, approver);
    }

    private void refuseAddRegionRequest(CareTakerRequest careTakerRequest, User approver) {
        approvalLogRepository.save(ApprovalLog.builder()
                .requestType(RequestType.REGION)
                .requestIdx(careTakerRequest.getIdx())
                .requestStatus(RequestStatus.REFUSE)
                .build()
                .setApprover(approver));
        careTakerRequest.refuse();
    }

    private void approveAddRegionRequest(CareTakerRequest careTakerRequest, User approver) {
        careTakerRequest.approve();
        if(careTakerRequest.getSubRegion1() != null){
            careTakerRequest.getWriter().addSubRegion1(careTakerRequest.getSubRegion1());
        }
        else if(careTakerRequest.getSubRegion2() != null){
            careTakerRequest.getWriter().addSubRegion2(careTakerRequest.getSubRegion2());
        }
        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(careTakerRequest.getIdx())
                .requestType(RequestType.REGION)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(approver));
    }

    private int getRandomCode() {
        return (int) Math.floor(Math.random() * 1000000);
    }

}
