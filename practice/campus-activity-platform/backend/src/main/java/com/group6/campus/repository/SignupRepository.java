package com.group6.campus.repository;

import com.group6.campus.entity.Signup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupRepository extends JpaRepository<Signup, Long> {
    Optional<Signup> findByUserIdAndActivityId(Long userId, Long activityId);
    Optional<Signup> findByIdAndStatus(Long id, String status);
    Page<Signup> findByActivityIdAndStatusOrderByIdAsc(Long activityId, String status, Pageable pageable);
    Page<Signup> findByUserIdAndStatusOrderByIdDesc(Long userId, String status, Pageable pageable);
}
