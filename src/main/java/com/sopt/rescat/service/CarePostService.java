package com.sopt.rescat.service;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.dto.response.CarePostDto;
import com.sopt.rescat.repository.CarePostRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CarePostService {
    private CarePostRepository carePostRepository;

    public CarePostService(final CarePostRepository carePostRepository) {
        this.carePostRepository = carePostRepository;
    }

    public Iterable<CarePostDto> findAllBy(Integer type) {
        return carePostRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }

    public Iterable<CarePost> findAll() {
        return carePostRepository.findByOrderByCreatedAtDesc();
    }

    public Iterable<CarePostDto> find5Post() {
        return carePostRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(CarePost::toCarePostDto)
                .collect(Collectors.toList());
    }
}
