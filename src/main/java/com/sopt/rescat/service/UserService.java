package com.sopt.rescat.service;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.exception.*;
import com.sopt.rescat.repository.CareTakerRequestRepository;
import com.sopt.rescat.repository.ProjectFundingLogRepository;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Slf4j
@Service
public class UserService {

    private final Integer CONFIRM = 1;
    private final Integer DEFER = 0;
    private final Integer REFUSE = 2;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ProjectFundingLogRepository projectFundingLogRepository;
    private final CareTakerRequestRepository careTakerRequestRepository;
    private final S3FileService s3FileService;
    private final RegionRepository regionRepository;
    private final MapService mapService;

    @Value("${GABIA.SMSPHONENUMBER}")
    private String ADMIN_PHONE_NUMBER;
    @Value("${GABIA.SMSID}")
    private String smsId;
    @Value("${GABIA.APIKEY}")
    private String apiKey;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
                       final CareTakerRequestRepository careTakerRequestRepository, S3FileService s3FileService,
                       final RegionRepository regionRepository, final MapService mapService,
                       final ProjectFundingLogRepository projectFundingLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.s3FileService = s3FileService;
        this.regionRepository = regionRepository;
        this.mapService = mapService;
        this.projectFundingLogRepository = projectFundingLogRepository;
    }

    public Boolean isExistingId(String id) {
        if (userRepository.findById(id).isPresent()) {
            throw new AlreadyExistsException("id", "이미 사용중인 ID입니다.");
        }
        return Boolean.FALSE;
    }

    public Boolean isExistingNickname(String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new AlreadyExistsException("nickname", "이미 사용중인 Nickname입니다.");
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

    public Map<String, Map<String, List<Region>>> getAllRegionList() {
        List<Region> allRegions = regionRepository.findAll();

        Map<String, Map<String, List<Region>>> allRegionList =
                allRegions.stream().collect(Collectors.groupingBy(Region::getSdName, Collectors.groupingBy(Region::getSggName, Collectors.toList())));

        return allRegionList;
    }

    @Transactional
    public void saveCareTakerRequest(final User user, CareTakerRequest careTakerRequest) throws IOException {
        Region region = regionRepository.findByEmdCode(careTakerRequest.getEmdCode())
                .orElseThrow(() -> new NotFoundException("emdCode", "지역을 찾을 수 없습니다."));

        careTakerRequestRepository.save(CareTakerRequest.builder().authenticationPhotoUrl(careTakerRequest.getAuthenticationPhotoUrl())
                .isConfirmed(DEFER).mainRegion(region).name(careTakerRequest.getName()).phone(careTakerRequest.getPhone()).writer(user).build());
    }

    public List<RegionDto> getRegionList(final User user) {

        List<Region> regions = new ArrayList<>();
        regions.add(user.getMainRegion());
        regions.add(user.getSubRegion1());
        regions.add(user.getSubRegion2());

        return regions.stream().filter(Objects::nonNull)
                .map(region -> region.toRegionDto())
                .collect(Collectors.toList());
    }

    private int getRandomCode() {
        return (int) Math.floor(Math.random() * 1000000);
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
}
