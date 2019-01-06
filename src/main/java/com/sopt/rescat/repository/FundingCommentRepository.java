package com.sopt.rescat.repository;

import com.sopt.rescat.domain.FundingComment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FundingCommentRepository extends CrudRepository<FundingComment, Long> {
    List<FundingComment> findByFundingIdxOrderByCreatedAtAsc(Long idx);
}
