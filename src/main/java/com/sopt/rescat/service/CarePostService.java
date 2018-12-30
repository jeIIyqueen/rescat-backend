package com.sopt.rescat.service;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.domain.CarePostComment;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import com.sopt.rescat.dto.request.CarePostRequestDto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.CarePostPhotoRepository;
import com.sopt.rescat.repository.CarePostRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarePostService {
    private CarePostRepository carePostRepository;
    private CarePostPhotoRepository carePostPhotoRepository;

    public CarePostService(final CarePostRepository carePostRepository, CarePostPhotoRepository carePostPhotoRepository) {
        this.carePostRepository = carePostRepository;
        this.carePostPhotoRepository = carePostPhotoRepository;
    }

    public void create(CarePostRequestDto carePostRequestDto) {
        carePostRepository.save(
                carePostRequestDto.toCarePost()
                        .initPhotos(carePostRequestDto.getPhotoUrls().stream()
                                .map((url) -> carePostPhotoRepository.save(new CarePostPhoto(url)))
                                .collect(Collectors.toList())));
    }

    public Iterable<CarePostResponseDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByOrderByCreatedAtDesc();
    }

    public Iterable<CarePostResponseDto> find5Post() {
        return carePostRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public CarePost findBy(Long idx) {
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

    public CarePost getCarePostBy(Long idx) {
        return carePostRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 글이 존재하지 않습니다."));
    }

    public List<Breed> getBreeds() {
        return Arrays.asList(Breed.values());
    }
}
