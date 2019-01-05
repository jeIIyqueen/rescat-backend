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
import org.json.JSONObject;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
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
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;


    @Value("${GABIA.SMSPHONENUMBER}")
    private String ADMIN_PHONE_NUMBER;
    @Value("${GABIA.SMSID}")
    private String smsId;
    @Value("${GABIA.APIKEY}")
    private String apiKey;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
                       final CareTakerRequestRepository careTakerRequestRepository, final ProjectFundingLogRepository projectFundingLogRepository,
                       final RegionRepository regionRepository, final ApprovalLogRepository approvalLogRepository,
                       final NotificationService notificationService, final NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.regionRepository = regionRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    public Boolean isExistingId(String id) {
        if(!id.matches(ID_REGEX))
            throw new InvalidValueException("id", "아이디는 영문자로 시작하는 6~20자 영문자 또는 숫자이어야 합니다.");

        if (userRepository.findById(id).isPresent()) {
            throw new AlreadyExistsException("id", "이미 사용중인 ID 입니다.");
        }
        return Boolean.FALSE;
    }

    public Boolean isExistingNickname(String nickname) {
        if(!nickname.matches(NICKNAME_REGEX))
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
                .build();
    }

    @Transactional
    public void saveCareTakerRequest(final User user, CareTakerRequest careTakerRequest) {
        Region region = regionRepository.findByEmdCode(careTakerRequest.getEmdCode())
                .orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        careTakerRequestRepository.save(CareTakerRequest.builder().authenticationPhotoUrl(careTakerRequest.getAuthenticationPhotoUrl())
                .isConfirmed(RequestStatus.DEFER.getValue()).mainRegion(region).name(careTakerRequest.getName()).phone(careTakerRequest.getPhone()).writer(user).build());
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

    private List<Funding> getFundingsByLogs(List<ProjectFundingLog> projectFundingLogs){
        return projectFundingLogs.stream()
                .map(ProjectFundingLog::getFunding)
                .distinct()
                .collect(Collectors.toList());
    }

    public UserMypageDto getEditUser(User user){
        return UserMypageDto.builder()
                .id(user.getId())
                .build();
    }

    @Transactional
    public UserMypageDto editUser(User user, UserEditDto userEditDto){
        User tokenUser = userRepository.findByIdx(user.getIdx());
        String editNickname = userEditDto.getNickname();

        if(tokenUser.getRole() == Role.MEMBER){
            if(!isExistingNickname(editNickname)){
                user.updateUser(editNickname, null);
            }
        }
        else if(tokenUser.getRole() == Role.CARETAKER){
            if(!isExistingNickname(editNickname)){
                user.updateUser(userEditDto.getNickname(), userEditDto.getPhone());
            }
        }
        return UserMypageDto.builder()
                .id(user.getId())
                .build();
    }

    @Transactional
    public void editUserPassword(User user, UserPasswordDto userPasswordDto){

        if(!passwordEncoder.matches(userPasswordDto.getPassword(), user.getPassword()))
            throw new NotMatchException("password", "비밀번호가 틀렸습니다.");

        if(userPasswordDto.getPassword().equals(userPasswordDto.getNewPassword()))
            throw new AlreadyExistsException("newPassword", "현재 사용중인 PASSWORD입니다.");
        if(userPasswordDto.checkValidPassword())
            user.updatePassword(passwordEncoder.encode(userPasswordDto.getNewPassword()));
    }

    @Transactional
    public void approveCareTaker(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CareTakerRequest careTakerRequest = careTakerRequestRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 요청이 존재하지 않습니다."));

        // 거절일 경우
        if(status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCareTakerRequest(careTakerRequest, approver);

            Notification notification = Notification.builder()
                    .receivingUser(approver)
                    .contents(careTakerRequest.getWriter() + "님의 케어테이커 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();
            notificationRepository.save(notification);

            notificationService.writePush(notification);

            return;
        }

        // 승인일 경우
        approveCareTakerRequest(careTakerRequest, approver);

        Notification notification =  Notification.builder()
                .receivingUser(approver)
                .contents(careTakerRequest.getWriter() + "님의 케어테이커 신청이 승인되었습니다. 앞으로 활발한 활동 부탁드립니다.")
                .build();
        notificationRepository.save(notification);

        notificationService.writePush(notification);

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

    private int getRandomCode() {
        return (int) Math.floor(Math.random() * 1000000);
    }
}
