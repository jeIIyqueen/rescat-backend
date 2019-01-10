package com.sopt.rescat.service;


import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.*;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiClass;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiResult;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
                       final NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.regionRepository = regionRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.projectFundingLogRepository = projectFundingLogRepository;
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
        savedUser.updateInstanceToken(userLoginDto.getInstanceToken());

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
                .isFinished(!careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmedAndType(user, RequestStatus.DEFER.getValue(), 0))
                .build();
    }

    public Integer getCareTakerRequestCount() {
        return careTakerRequestRepository.countByIsConfirmed(RequestStatus.DEFER.getValue());
    }

    @Transactional
    public void saveCareTakerRequest(final User user, CareTakerRequest careTakerRequest) {

        if (careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmedAndType(user, RequestStatus.DEFER.getValue(), 0))
            throw new AlreadyExistsException("careTakerRequest", "아직 완료되지 않은 케어테이커 신청이 있습니다.");
        if (careTakerRequestRepository.existsCareTakerRequestByWriterAndIsConfirmedAndType(user, RequestStatus.CONFIRM.getValue(), 0))
            throw new AlreadyExistsException("careTakerRequest", "이미 케어테이커 인증이 완료됐습니다.");

        String[] fullName = careTakerRequest.getRegionFullName().split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region region = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));


        careTakerRequestRepository.save(CareTakerRequest.builder()
                .authenticationPhotoUrl(careTakerRequest.getAuthenticationPhotoUrl())
                .isConfirmed(RequestStatus.DEFER.getValue())
                .region(region)
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
        return careTakerRequestRepository.findAllByIsConfirmedOrderByCreatedAtDesc(RequestStatus.DEFER.getValue())
                .stream().peek(careTakerRequest -> {
                    careTakerRequest.fillUserNickname();
                    careTakerRequest.fillRegionFullName();
                })
                .collect(Collectors.toList());
    }

    public List<FundingResponseDto> getSupportingFundings(User user) {
        List<ProjectFundingLog> projectFundingLogs = projectFundingLogRepository.findBySponsorOrderByCreatedAtDesc(user);
        return getFundingsBy(projectFundingLogs);
    }

    private List<FundingResponseDto> getFundingsBy(List<ProjectFundingLog> projectFundingLogs) {
        return projectFundingLogs.stream()
                .map(projectFundingLog -> projectFundingLog.getFunding().toFundingDto())
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

        User writer = careTakerRequest.getWriter();

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue()))
            refuseCareTakerRequest(careTakerRequest, approver);

        // 승인일 경우
        else if (status.equals(RequestStatus.CONFIRM.getValue()))
            approveCareTakerRequest(careTakerRequest, approver);

        notificationService.send(careTakerRequest, careTakerRequest.getWriter());
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
        careTakerRequest.getWriter().grantCareTakerAuth(careTakerRequest.getPhone(), careTakerRequest.getName(), careTakerRequest.getRegion());
        approvalLogRepository.save(ApprovalLog.builder()
                .requestIdx(careTakerRequest.getIdx())
                .requestType(RequestType.CARETAKER)
                .requestStatus(RequestStatus.CONFIRM)
                .build()
                .setApprover(approver));
    }

    private List<Region> getUserRegionList(User user) {
        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());
        return regions;
    }

    @Transactional
    public void deleteRegion(User user, String regionFullName) {

        List<Region> regions = getUserRegionList(user);
        regions.removeAll(Collections.singleton(null));

        if (regions.size() == 1)
            throw new InvalidValueException("regionFullName", "지역은 최소 1개 이상이어야 합니다.");

        String[] fullName = regionFullName.split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region deleteRegion = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        if (!getUserRegionList(user).contains(deleteRegion))
            throw new NotMatchException("mainRegion or subRegion1 or subRegion2", "유저에게 존재하지 않는 지역입니다.");
        else if (deleteRegion.equals(user.getMainRegion()))
            user.deleteMainRegion(user.getSubRegion1(), user.getSubRegion2());
        else if (deleteRegion.equals(user.getSubRegion1()))
            user.deleteSubRegion1(user.getSubRegion2());
        else if (deleteRegion.equals(user.getSubRegion2()))
            user.deleteSubRegion2();
    }

    @Transactional
    public void saveAddRegionRequest(final User user, UserAddRegionDto userAddRegionDto) {

        String[] fullName = userAddRegionDto.getRegionFullName().split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region addRegion = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        if (getUserRegionList(user).contains(addRegion))
            throw new AlreadyExistsException("regionFullName", "유저에게 이미 존재하는 지역입니다.");

        careTakerRequestRepository.save(CareTakerRequest.builder()
                .authenticationPhotoUrl(userAddRegionDto.getAuthenticationPhotoUrl())
                .isConfirmed(RequestStatus.DEFER.getValue())
                .region(addRegion)
                .name(user.getName())
                .phone(user.getPhone())
                .writer(user)
                .type(1)
                .build());
    }

    //지역 추가 (관리자 승인 X)
    @Transactional
    public void saveAddRegion(final User user, String regionFullName) {
        String[] fullName = regionFullName.split(" ");
        if (fullName.length != 3)
            throw new InvalidValueException("regionFullName", "유효한 지역이름을 입력해주세요.");
        Region addRegion = regionRepository.findBySdNameAndSggNameAndEmdName(fullName[0], fullName[1], fullName[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        if (getUserRegionList(user).contains(addRegion))
            throw new AlreadyExistsException("regionFullName", "유저에게 이미 존재하는 지역입니다.");

        if (user.getMainRegion() == null) {
            user.addMainRegion(addRegion);
        } else if (user.getMainRegion() != null && user.getSubRegion1() == null) {
            user.addSubRegion1(addRegion);
        } else if (user.getMainRegion() != null && user.getSubRegion1() != null && user.getSubRegion2() == null) {
            user.addSubRegion2(addRegion);
        }
    }

    @Transactional
    public void editUserRegion(User user, List<String> editRegions) {

        String[] fullName0 = editRegions.get(0).split(" ");
        String[] fullName1 = editRegions.get(1).split(" ");

        Region editRegion0 = regionRepository.findBySdNameAndSggNameAndEmdName(fullName0[0], fullName0[1], fullName0[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));
        Region editRegion1 = regionRepository.findBySdNameAndSggNameAndEmdName(fullName1[0], fullName1[1], fullName1[2])
                .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));

        user.updateRegions(editRegion0, editRegion1, null);

        if(editRegions.size() == 3) {
            String[] fullName2 = editRegions.get(2).split(" ");
            Region editRegion2 = regionRepository.findBySdNameAndSggNameAndEmdName(fullName2[0], fullName2[1], fullName2[2])
                    .orElseThrow(() -> new NotFoundException("regionFullName", "지역을 찾을 수 없습니다."));
            user.updateRegions(editRegion0, editRegion1, editRegion2);
        }
    }

    @Transactional
    public void confirmedAddRegion(Long idx, @Range(min = 1, max = 2) Integer status, User approver) {
        CareTakerRequest careTakerRequest = careTakerRequestRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 요청이 존재하지 않습니다."));

        User writer = careTakerRequest.getWriter();

        // 거절일 경우
        if (status.equals(RequestStatus.REFUSE.getValue())) {
            refuseAddRegionRequest(careTakerRequest, approver);
        }else {//승인일경우
            approveAddRegionRequest(careTakerRequest, approver);
        }

        notificationService.send(careTakerRequest, careTakerRequest.getWriter());
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
        if (careTakerRequest.getWriter().getMainRegion() == null) {
            careTakerRequest.getWriter().addMainRegion(careTakerRequest.getRegion());
        } else if (careTakerRequest.getWriter().getMainRegion() != null && careTakerRequest.getWriter().getSubRegion1() == null) {
            careTakerRequest.getWriter().addSubRegion1(careTakerRequest.getRegion());
        } else if (careTakerRequest.getWriter().getMainRegion() != null && careTakerRequest.getWriter().getSubRegion1() != null && careTakerRequest.getWriter().getSubRegion2() == null) {
            careTakerRequest.getWriter().addSubRegion2(careTakerRequest.getRegion());
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
