package com.sopt.rescat.repository;

import com.sopt.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findById(String id);

    Optional<User> findByNickname(String nickname);

    User findByIdx(Long idx);
}
