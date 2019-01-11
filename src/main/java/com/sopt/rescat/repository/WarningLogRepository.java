package com.sopt.rescat.repository;

import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.log.WarningLog;
import com.sopt.rescat.domain.enums.WarningType;
import org.springframework.data.repository.CrudRepository;

public interface WarningLogRepository extends CrudRepository<WarningLog, Long> {
    boolean existsWarningLogByWarningIdxAndWarningTypeAndWarningUser(Long carePostIdx, WarningType warningType, User user);
}
