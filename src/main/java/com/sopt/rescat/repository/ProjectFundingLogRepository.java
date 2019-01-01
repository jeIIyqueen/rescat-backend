package com.sopt.rescat.repository;

import com.sopt.rescat.domain.Funding;
import com.sopt.rescat.domain.ProjectFundingLog;
import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectFundingLogRepository extends CrudRepository<ProjectFundingLog, Long> {

    List<ProjectFundingLog> findBySponsorOrderByCreatedAtDesc(User user);
}
