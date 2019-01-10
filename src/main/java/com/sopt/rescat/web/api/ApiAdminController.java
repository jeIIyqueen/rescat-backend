package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.*;
import com.sopt.rescat.dto.ExceptionDto;
import com.sopt.rescat.dto.JwtTokenDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.dto.response.FundingResponseDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.service.*;
import com.sopt.rescat.utils.HttpSessionUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@Api(value = "ApiAdminController", description = "관리자페이지 관련 api")
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {
    private UserService userService;
    private FundingService fundingService;
    private CarePostService carePostService;
    private MapService mapService;
    private JWTService jwtService;

    @Value("${NAVER.MAP.REVERSE.CLIENTID}")
    private String clientId;
    @Value("${NAVER.MAP.REVERSE.CLIENTSECRETE}")
    private String clientSecret;

    public ApiAdminController(final UserService userService,
                              final FundingService fundingService,
                              final CarePostService carePostService,
                              final MapService mapService, JWTService jwtService) {
        this.userService = userService;
        this.fundingService = fundingService;
        this.carePostService = carePostService;
        this.mapService = mapService;
        this.jwtService = jwtService;
    }

    @ApiOperation(value = "유저 로그인", notes = "유저가 로그인합니다. 성공시 jwt 토큰을 바디에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "로그인 성공"),
            @ApiResponse(code = 401, message = "로그인 실패", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody UserLoginDto userLoginDto,
            HttpSession session) {
        HttpSessionUtils.setTokenInSession(session, JwtTokenDto.builder()
                .token(jwtService.create(userService.login(userLoginDto).getIdx()))
                .build()
                .getToken()
        );
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "홈화면 요청 개수 리스트 조회", notes = "홈화면 요청 개수 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "홈화면 요청 개수 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/home/counts")
    public ResponseEntity<Map<String, Integer>> getRequestCounts(HttpSession session) {
        HttpSessionUtils.checkAdminUser(session);

        Map<String, Integer> body = new HashMap<>();
        body.put("careTakerRequest", userService.getCareTakerRequestCount());
        body.put("carePostRequest", carePostService.getCarePostRequestCount());
        body.put("fundingRequest", fundingService.getFundingCount());
        body.put("mapMarkerRequest", mapService.getMarkerRequestCount());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ApiOperation(value = "케어테이커 인증요청, 마이페이지에서의 지역 추가요청 리스트 조회", notes = "케어테이커 인증요청, 지역 추가요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "케어테이커 인증요청, 지역추가요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/care-taker-requests")
    public ResponseEntity<Iterable<CareTakerRequest>> showCareTakerRequest(HttpSession session) {
        HttpSessionUtils.checkAdminUser(session);
        return ResponseEntity.status(HttpStatus.OK).body(userService.getCareTakerRequest());
    }

    @ApiOperation(value = "케어테이커 인증요청 승인/거절", notes = "케어테이커 인증요청을 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "케어테이커 인증요청 처리 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/care-taker-requests/{idx}")
    public ResponseEntity<Void> approveCareTaker(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpSession httpSession) {
        if (!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        userService.approveCareTaker(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "케어테이커 지역 추가요청 승인/거절", notes = "케어테이커 지역 추가요청을 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "케어테이커 지역 추가요청 처리 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/add-region-requests/{idx}")
    public ResponseEntity<Void> approveAddRegion(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpSession httpSession) {
        if (!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        userService.confirmedAddRegion(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @ApiOperation(value = "크라우드 펀딩 글 게시요청 리스트 조회", notes = "크라우드 펀딩 글 게시요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 게시요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/funding-requests")
    public ResponseEntity showFundingRequest() {
        return ResponseEntity.status(HttpStatus.OK).body(fundingService.getFundingRequests());
    }

    @ApiOperation(value = "크라우드 펀딩 글 승인/거절", notes = "idx 에 따른 크라우드 펀딩 글 게시를 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "크라우드 펀딩 글 게시요청 처리 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/funding-requests/{idx}")
    public ResponseEntity<FundingResponseDto> confirmFundingPost(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpSession httpSession) {
        if (!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(fundingService.confirmFunding(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver));
    }


    @ApiOperation(value = "입양/임시보호 글 게시요청 리스트 조회", notes = "입양/임시보호 글 게시요청 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 게시요청 리스트 반환 성공"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/care-post-requests")
    public ResponseEntity<Iterable<CarePost>> showCarePostRequest() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.getCarePostRequests());
    }

    @ApiOperation(value = "입양/임시보호 글 승인/거절", notes = "idx 에 따른 입양/임시보호 글 게시를 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 게시요청 처리 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "권한 미보유"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/care-post-requests/{idx}")
    public ResponseEntity<CarePostResponseDto> confirmCarePost(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpSession httpSession) {
        if (!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(carePostService.confirmCarePost(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver));
    }


    @ApiOperation(value = "맵 마커 수정/등록 리스트 조회", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = MapRequest.class),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/map-requests")
    public ResponseEntity<Iterable<MapRequest>> showMapRequest() {
        return ResponseEntity.status(HttpStatus.OK).body(mapService.getMapRequest());
    }

    @ApiOperation(value = "맵 마커 수정/등록 요청 승인/거절", notes = "고양이, 배식소, 병원 마커의 등록 또는 수정 요청을 승인/거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "승인 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "요청 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PutMapping("/map-requests/{idx}")
    public ResponseEntity<MapRequest> approveMapRequest(
            @PathVariable Long idx,
            @ApiParam(value = "1: 승인, 2: 거절/ example -> {\"status\": 1}")
            @RequestBody Map<String, Object> body,
            HttpSession httpSession) {
        if (!body.containsKey("status"))
            throw new InvalidValueException("status", "body 의 status 값이 존재하지 않습니다.");

        User approver = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(mapService.approveMap(idx, Integer.parseInt(String.valueOf(body.get("status"))), approver));
    }

    @ApiOperation(value = "고양이 마커 생성", notes = "고양이 마커를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "마커 생성 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/map-markers/cats")
    public ResponseEntity<Void> create(
            @Valid @RequestBody Cat cat,
            HttpSession httpSession) {
        User admin = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        mapService.create(cat, admin);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "배식소 또는 병원 마커 생성", notes = "배식소 또는 병원 마커를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "마커 생성 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러", response = ExceptionDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @PostMapping("/map-markers/places")
    public ResponseEntity<Void> create(
            @Valid @RequestBody Place place,
            HttpSession httpSession) {
        User admin = HttpSessionUtils.getAdminUserIfPresent(httpSession);
        mapService.create(place, admin);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "경위도 좌표 -> 주소 변환 (reverse geocoding)")
    @GetMapping("/geo-coding-reverse")
    public ResponseEntity<String> getCoordsToAddrJsonResult(@RequestParam Double lng, @RequestParam Double lat) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-NCP-APIGW-API-KEY-ID", clientId);
        httpHeaders.add("X-NCP-APIGW-API-KEY", clientSecret);

        String url = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" + lng + "," + lat + "&sourcecrs=epsg:4326&output=json&orders=addr,admcode";

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(httpHeaders), String.class);
    }


}
