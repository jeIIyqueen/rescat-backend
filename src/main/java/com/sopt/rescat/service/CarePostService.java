package com.sopt.rescat.service;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.CareApplication;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.AlreadyExistsException;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotFoundException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.CareApplicationRepository;
import com.sopt.rescat.repository.CarePostRepository;
import com.sopt.rescat.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarePostService {

    private CarePostRepository carePostRepository;
    private CareApplicationRepository careApplicationRepository;
    private UserRepository userRepository;

    public CarePostService(final CarePostRepository carePostRepository,
                           final CareApplicationRepository careApplicationRepository,
                           final UserRepository userRepository) {
        this.carePostRepository = carePostRepository;
        this.careApplicationRepository = careApplicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        CarePost carePost = carePostRepository.save(carePostRequestDto.toCarePost(false)
                .setWriter(loginUser));
        carePost.initPhotos(carePostRequestDto.convertPhotoUrlsToCarePostPhoto(carePost));
    }

    public Iterable<CarePostResponseDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeAndIsConfirmedOrderByCreatedAtDesc(type, RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByIsConfirmedOrderByCreatedAtDesc(RequestStatus.CONFIRM.getValue());
    }

    public Iterable<CarePostResponseDto> find5Post() {
        return carePostRepository.findTop5ByIsConfirmedOrderByCreatedAtDesc(RequestStatus.CONFIRM.getValue()).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public CarePost findCarePostBy(Long idx) {
        return getCarePostBy(idx).setWriterNickname();
    }

    public List<CarePostComment> findCommentsBy(Long idx) {
        return getCarePostBy(idx)
                .getComments().stream()
                .peek((carePostComment) -> {
                    carePostComment.setUserRole();
                    carePostComment.setWriterNickname();
                }).collect(Collectors.toList());
    }

    @Transactional
    public void confirmPost(Long idx) {
        findCarePostBy(idx).updateConfirmStatus(RequestStatus.CONFIRM.getValue());
    }

    private CarePost getCarePostBy(Long idx) {
        return carePostRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }

    public List<Breed> getBreeds() {
        return Arrays.asList(Breed.values());
    }


    public Iterable<CarePost> findAllByUser(User user) {
        return carePostRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, RequestStatus.CONFIRM.getValue());
    }

    @Transactional
    public void createCareApplication(CareApplication careApplication, User loginUser, Long carePostIdx) {
        CarePost carePost = carePostRepository.findById(carePostIdx).orElseThrow(() -> new NotFoundException("idx", "관련 글을 찾을 수 없습니다."));
        carePost.isFinished();
        carePost.equalsWriter(loginUser);
        carePost.isSubmitted(loginUser);

        careApplicationRepository.save(
                CareApplication.builder().address(careApplication.getAddress()).birth(careApplication.getBirth())
                .carePost(carePost).companionExperience(careApplication.getCompanionExperience())
                .finalWord(careApplication.getFinalWord()).houseType(careApplication.getHouseType())
                .job(careApplication.getJob()).name(careApplication.getName()).phone(careApplication.getPhone())
                .writer(loginUser).type(careApplication.getType()).isAccepted(false).build());
    }

    @Transactional
    public void acceptCareApplication(Long careApplicationIdx, User loginUser){
        CareApplication careApplication = careApplicationRepository.findById(careApplicationIdx).orElseThrow(() -> new NotFoundException("idx", "신청서를 찾을 수 없습니다."));
        careApplication.getCarePost().isFinished();

        careApplication.accept(loginUser);
        careApplication.getCarePost().finish();
    }
}