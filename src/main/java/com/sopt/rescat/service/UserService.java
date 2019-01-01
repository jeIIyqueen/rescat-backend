package com.sopt.rescat.service;

import com.sopt.rescat.domain.ApprovalLog;
import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.RegionDto;
import com.sopt.rescat.dto.UserJoinDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.dto.UserMypageDto;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.ApprovalLogRepository;
import com.sopt.rescat.repository.CareTakerRequestRepository;
import com.sopt.rescat.repository.RegionRepository;
import com.sopt.rescat.repository.UserRepository;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiClass;
import com.sopt.rescat.utils.gabia.com.gabia.api.ApiResult;
import com.sopt.rescat.vo.AuthenticationCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CareTakerRequestRepository careTakerRequestRepository;
    private final RegionRepository regionRepository;
    private final ApprovalLogRepository approvalLogRepository;

    @Value("${GABIA.SMSPHONENUMBER}")
    private String ADMIN_PHONE_NUMBER;
    @Value("${GABIA.SMSID}")
    private String smsId;
    @Value("${GABIA.APIKEY}")
    private String apiKey;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
                       final CareTakerRequestRepository careTakerRequestRepository,
                       final RegionRepository regionRepository, final ApprovalLogRepository approvalLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.regionRepository = regionRepository;
        this.approvalLogRepository = approvalLogRepository;
    }

    public Boolean isExistingId(String id) {
        if (userRepository.findById(id).isPresent()) {
            throw new AlreadyExistsException("id", "이미 사용중인 ID 입니다.");
        }
        return Boolean.FALSE;
    }

    public Boolean isExistingNickname(String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new AlreadyExistsException("nickname", "이미 사용중인 Nickname 입니다.");
        }
        return Boolean.FALSE;
    }

    public User create(UserJoinDto userJoinDto) {
        isExistingId(userJoinDto.getId());
        isExistingNickname(userJoinDto.getNickname());
        return userRepository.save(userJoinDto.toUser(passwordEncoder.encode(userJoinDto.getPassword())));
    }

    public User login(UserLoginDto userLoginDto) {
        User savedUser = userRepository.findById(userLoginDto.getId())
                .orElseThrow(() -> new UnAuthenticationException("id", "해당 ID를 가진 사용자가 존재하지 않습니다."));
        savedUser.matchPasswordBy(userLoginDto, passwordEncoder);
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

    public UserMypageDto getUserMypage(User user){
        List<RegionDto> regions = getRegionList(user);
        return new UserMypageDto(user, regions);
    }

    @Transactional
    public void saveCareTakerRequest(final User user, CareTakerRequest careTakerRequest) {
        Region region = regionRepository.findByEmdCode(careTakerRequest.getEmdCode())
                .orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        careTakerRequestRepository.save(CareTakerRequest.builder().authenticationPhotoUrl(careTakerRequest.getAuthenticationPhotoUrl())
                .isConfirmed(RequestStatus.CONFIRM.getValue()).mainRegion(region).name(careTakerRequest.getName()).phone(careTakerRequest.getPhone()).writer(user).build());
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

    @Transactional
    public void approveCareTaker(Long idx, Integer status, User approver) {
        CareTakerRequest careTakerRequest = careTakerRequestRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 요청이 존재하지 않습니다."));

        // 거절일 경우
        if(status.equals(RequestStatus.REFUSE.getValue())) {
            refuseCareTakerRequest(careTakerRequest, approver);
            return;
        }

        // 승인일 경우
        approveCareTakerRequest(careTakerRequest, approver);
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
        careTakerRequest.getWriter().grantCareTakerAuth(careTakerRequest.getPhone(), careTakerRequest.getName());
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
