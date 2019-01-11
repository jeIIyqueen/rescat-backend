package com.sopt.rescat.repository;

import com.sopt.rescat.domain.log.ApprovalLog;
import org.springframework.data.repository.CrudRepository;

public interface ApprovalLogRepository extends CrudRepository<ApprovalLog, Long> {
}
