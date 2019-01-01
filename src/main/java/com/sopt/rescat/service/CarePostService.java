package com.sopt.rescat.service;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.CarePostPhotoRepository;
import com.sopt.rescat.repository.CarePostRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarePostService {
    private final Integer CONFIRM = 1;
    private final Integer DEFER   = 0;
    private final Integer REFUSE  = 2;

    private CarePostRepository carePostRepository;
    private CarePostPhotoRepository carePostPhotoRepository;

    public CarePostService(final CarePostRepository carePostRepository, CarePostPhotoRepository carePostPhotoRepository) {
        this.carePostRepository = carePostRepository;
        this.carePostPhotoRepository = carePostPhotoRepository;
    }

    @Transactional
    public void create(CarePostRequestDto carePostRequestDto, User loginUser) {
        CarePost carePost = carePostRepository.save(carePostRequestDto.toCarePost()
                .setWriter(loginUser));

        carePost.initPhotos(carePostRequestDto.convertPhotoUrlsToCarePostPhoto(carePost));
    }

    public Iterable<CarePostResponseDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeAndIsConfirmedOrderByCreatedAtDesc(type, CONFIRM).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByIsConfirmedOrderByCreatedAtDesc(CONFIRM);
    }

    public Iterable<CarePostResponseDto> find5Post() {
        return carePostRepository.findTop5ByIsConfirmedOrderByCreatedAtDesc(CONFIRM).stream()
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
        findCarePostBy(idx).updateConfirmStatus(CONFIRM);
    }

    private CarePost getCarePostBy(Long idx) {
        return carePostRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }

    public List<Breed> getBreeds() {
        return Arrays.asList(Breed.values());
    }


    public Iterable<CarePost> findAllByUser(User user) {
        return carePostRepository.findByWriterAndIsConfirmedOrderByCreatedAtDesc(user, CONFIRM);
    }
}
