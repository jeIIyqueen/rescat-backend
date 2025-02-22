package jellyqueen.rescat.web.api;

import jellyqueen.rescat.domain.CareApplication;
import jellyqueen.rescat.domain.CarePost;
import jellyqueen.rescat.domain.CarePostComment;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.dto.request.CarePostRequestDto;
import jellyqueen.rescat.dto.response.CarePostResponseDto;
import jellyqueen.rescat.service.CarePostService;
import jellyqueen.rescat.service.JWTService;
import jellyqueen.rescat.service.UserService;
import jellyqueen.rescat.utils.auth.Auth;
import jellyqueen.rescat.utils.auth.AuthAspect;
import jellyqueen.rescat.utils.auth.CareTakerAuth;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api(value = "ApiCarePostController", description = "입양/임시보호 글 관련 api")
@RestController
@RequestMapping("/api/care-posts")
public class ApiCarePostController {

    private CarePostService carePostService;
    private UserService userService;
    private JWTService jwtService;

    public ApiCarePostController(CarePostService carePostService,
                                 UserService userService,
                                 JWTService jwtService) {
        this.carePostService = carePostService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @ApiOperation(value = "입양 글 리스트 또는 임시보호 글 리스트 조회", notes = "입양 글 리스트 또는 임시보호 글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양 글 리스트 또는 임시보호 글 리스트 반환 성공", response = CarePostResponseDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("")
    public ResponseEntity<Iterable<CarePostResponseDto>> getAllBy(
            @ApiParam(value = "0: 입양, 1: 임시보호", required = true)
            @RequestParam Integer type) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findAllBy(type));
    }

    @ApiOperation(value = "입양/임시보호 글 등록 요청", notes = "입양/임시보호 글 등록을 관리자에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 등록 성공"),
            @ApiResponse(code = 409, message = "완료되지 않은 글 중복 오류"),
            @ApiResponse(code = 400, message = "파라미터 형식 오류"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @Auth
    @PostMapping("")
    public ResponseEntity<Void> create(
            @RequestHeader(value = "Authorization") final String token,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CarePostRequestDto carePostRequestDto) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.create(carePostRequestDto, loginUser);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "입양/임시보호 글 조회", notes = "idx에 따른 입양/임시보호 글을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 반환 성공", response = CarePost.class),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", dataType = "string", paramType = "header")
    })
    @GetMapping("/{idx}")
    public ResponseEntity<CarePost> getPostByIdx(
            @RequestHeader(value = "Authorization") final Optional<String> token,
            @ApiParam(value = "글 번호", required = true) @PathVariable Long idx) {
        if (token.isPresent()) {
            User loginUser = userService.getUserBy(jwtService.decode(token.get()).getIdx());
            return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCarePostBy(idx, loginUser));
        }
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCarePostBy(idx, null));
    }

    @ApiOperation(value = "입양/임시보호 글의 댓글 조회", notes = "idx에 해당하는 입양/임시보호 글의 댓글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "idx에 해당하는 입양/임시보호 글의 댓글 리스트 반환 성공", response = CarePostComment.class),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", dataType = "string", paramType = "header")
    })
    @GetMapping("/{idx}/comments")
    public ResponseEntity<Iterable<CarePostComment>> getComments(
            @RequestHeader(value = "Authorization") final Optional<String> token,
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx) {
        if (token.isPresent()) {
            User loginUser = userService.getUserBy(jwtService.decode(token.get()).getIdx());
            return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCommentsBy(idx, loginUser));
        }
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findCommentsBy(idx, null));
    }

    @ApiOperation(value = "입양/임시보호 글의 댓글 등록", notes = "idx 에 따른 입양/임시보호 글의 댓글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "입양/임시보호 글의 댓글 등록 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "댓글 작성 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    @PostMapping("/{idx}/comments")
    @Auth
    public ResponseEntity<CarePostComment> createComment(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable Long idx,
            @RequestBody CarePostComment carePostComment,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        return ResponseEntity.status(HttpStatus.CREATED).body(carePostService.createComment(idx, carePostComment, loginUser));
    }

    @ApiOperation(value = "입양/임시보호 글의 댓글 삭제", notes = "idx 에 따른 입양/임시보호 글의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "입양/임시보호 펀딩 글의 댓글 삭제 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "댓글 삭제 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "idx", value = "carePostIdx", required = true, dataType = "long", paramType = "path")
    })
    @DeleteMapping("/{idx}/comments/{comment-idx}")
    @Auth
    public ResponseEntity<Void> createComment(
            @ApiParam(value = "글 번호", required = true)
            @PathVariable(name = "comment-idx") Long commentIdx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.deleteComment(commentIdx, loginUser);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "입양/임시보호 글 중 최신 5개 리스트 조회", notes = "입양/임시보호 글 중 최신 5개 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 중 최신 5개 리스트 반환 성공", response = CarePostResponseDto.class),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/main")
    public ResponseEntity<Iterable<CarePostResponseDto>> get5Post() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.find5Post());
    }

    @ApiOperation(value = "품종 리스트 조회", notes = "품종 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "품종 리스트 반환 성공"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @GetMapping("/breeds")
    public ResponseEntity<List<Map>> getBreeds() {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.getBreeds());
    }

    @ApiOperation(value = "입양/임시보호 신청", notes = "입양/임시보호 글에 대한 신청을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "신청 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "입양/임시보호 글 찾을 수 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @Auth
    @PostMapping("/{idx}/applications")
    public ResponseEntity<Void> createCareApplication(
            @RequestHeader(value = "Authorization") final String token,
            @ApiParam(value = "글 번호", required = true) @PathVariable Long idx,
            @RequestBody @Valid CareApplication careApplication,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.createCareApplication(careApplication, loginUser, idx);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiOperation(value = "입양/임시보호 신청 승낙", notes = "입양/임시보호 글에 대한 신청을 승낙합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "승낙 성공"),
            @ApiResponse(code = 400, message = "유효성 검사 에러"),
            @ApiResponse(code = 401, message = "권한 없음"),
            @ApiResponse(code = 404, message = "관련 글/신청 찾을 수 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header")
    })
    @CareTakerAuth
    @PostMapping("/applications/{idx}/accepting")
    public ResponseEntity<CareApplication> acceptCareApplication(
            @RequestHeader(value = "Authorization") final String token,
            @ApiParam(value = "신청 번호", required = true) @PathVariable Long idx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.acceptCareApplication(idx, loginUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "입양/임시보호 글 신고", notes = "idx에 따른 입양/임시보호 글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글 신고 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "글 신고 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", dataType = "string", paramType = "header")
    })
    @Auth
    @PostMapping("/{idx}/warning")
    public ResponseEntity<Void> warningCarePost(
            @ApiParam(value = "글 번호", required = true) @PathVariable Long idx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.warningCarePost(idx, loginUser);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "입양/임시보호 글의 댓글 신고", notes = "idx 에 따른 입양/임시보호 글의 댓글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "입양/임시보호 글의 댓글 신고 성공"),
            @ApiResponse(code = 400, message = "글번호에 해당하는 글 없음"),
            @ApiResponse(code = 401, message = "댓글 신고 권한 없음"),
            @ApiResponse(code = 500, message = "서버 에러")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "JWT Token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "idx", value = "carePostIdx", required = true, dataType = "long", paramType = "path")
    })
    @Auth
    @PostMapping("/{idx}/comments/{comment-idx}/warning")
    public ResponseEntity<Void> warningComment(
            @ApiParam(value = "댓글 번호", required = true)
            @PathVariable(name = "comment-idx") Long commentIdx,
            HttpServletRequest httpServletRequest) {
        User loginUser = (User) httpServletRequest.getAttribute(AuthAspect.USER_KEY);
        carePostService.warningCarePostComment(commentIdx, loginUser);
        return ResponseEntity.ok().build();
    }

}
