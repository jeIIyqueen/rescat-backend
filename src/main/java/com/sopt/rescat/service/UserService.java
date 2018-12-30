package com.sopt.rescat.service;

import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.*;
import com.sopt.rescat.exception.*;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
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

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder, final JWTService jwtService,
                       final CareTakerRequestRepository careTakerRequestRepository, S3FileService s3FileService,
                       final RegionRepository regionRepository, final MapService mapService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.careTakerRequestRepository = careTakerRequestRepository;
        this.s3FileService = s3FileService;
        this.regionRepository = regionRepository;
        this.mapService = mapService;
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

    private int getRandomCode() {
        return (int) Math.floor(Math.random() * 1000000);
    }

    public User getUser(final Long userIdx){
        User tokenUser = userRepository.findByIdx(userIdx);
        if (tokenUser == null) throw new UnAuthenticationException("token", "유효하지 않은 토큰입니다.");

        if(!(tokenUser.getRole() == Role.CARETAKER)){
            throw new UnAuthenticationException("user", "케어테이커 인증을 받지 않은 사용자입니다.");
        }
        return tokenUser;
    }

    @Transactional
    public void saveCareTakerRequest(Long idx, CareTakerRequestDto careTakerRequestDto) throws IOException {
        User tokenUser = userRepository.findByIdx(idx);
        if (tokenUser == null) throw new UnAuthenticationException("token", "유효하지 않은 토큰입니다.");

        if(!careTakerRequestDto.hasAuthenticationPhoto())
            throw new InvalidValueException("authenticationPhoto","authenticationPhoto가 존재하지 않습니다.");

        String authenticationPhotoUrl = s3FileService.upload(careTakerRequestDto.getAuthenticationPhoto());

        Region mainRegion = regionRepository.findByEmdCode(careTakerRequestDto.getEmdCode())
                .orElseThrow(() -> new NotFoundException("emdcode", "해당 지역을 찾을 수 없습니다."));

        careTakerRequestRepository.save(careTakerRequestDto.toCareTakerRequest(tokenUser, mainRegion, authenticationPhotoUrl));
    }

    public UserMypageDto getUserMypage(Long idx){
        User tokenUser = userRepository.findByIdx(idx);
        if (tokenUser == null) throw new UnAuthenticationException("token", "유효하지 않은 토큰입니다.");

        List<RegionDto> regions = mapService.getRegionList(tokenUser);

        UserMypageDto userMypageDto = new UserMypageDto(tokenUser, regions);

        return userMypageDto;
    }


}
