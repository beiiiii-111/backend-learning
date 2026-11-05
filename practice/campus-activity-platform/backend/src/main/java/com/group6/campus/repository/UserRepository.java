package com.group6.campus.repository;

import com.group6.campus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsDeleted(String username, Integer isDeleted);
    boolean existsByUsernameAndIsDeleted(String username, Integer isDeleted);
}
