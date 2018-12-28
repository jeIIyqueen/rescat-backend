package com.sopt.rescat.service;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.repository.CarePostRepository;
import org.springframework.stereotype.Service;

@Service
public class CarePostService {
    private CarePostRepository carePostRepository;

    public CarePostService(final CarePostRepository carePostRepository) {
        this.carePostRepository = carePostRepository;
    }

    public Iterable<CarePost> findAllBy(Integer type) {
        return carePostRepository.findByType(type);
    }
}
