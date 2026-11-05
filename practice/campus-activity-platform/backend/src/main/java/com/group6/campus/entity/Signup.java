package com.group6.campus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "signup",
       uniqueConstraints = @UniqueConstraint(name = "uk_signup_user_activity",
                                             columnNames = {"user_id", "activity_id"}))
public class Signup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    /** signed / cancelled（取消改状态不删行，可再次报名） */
    @Column(nullable = false, length = 20)
    private String status = "signed";

    @Column(name = "signup_time", nullable = false)
    private LocalDateTime signupTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;
}
